package dev.team.labslot.core.service;

import dev.team.labslot.core.constant.LaboratoryState;
import dev.team.labslot.core.constant.ReservationState;
import dev.team.labslot.core.constant.UserRole;
import dev.team.labslot.core.dto.ReservationDTO;
import dev.team.labslot.core.entity.Laboratory;
import dev.team.labslot.core.entity.Reservation;
import dev.team.labslot.core.entity.User;
import dev.team.labslot.core.port.LaboratoryRepository;
import dev.team.labslot.core.port.ReportGateway;
import dev.team.labslot.core.port.ReservationRepository;
import dev.team.labslot.core.port.UserRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final UserRepository userRepository;
    // Reloj con la zona America/Bogota; en los tests se pasa un reloj fijo
    private final Clock clock;
    // Genera el reporte PDF (el core no sabe que se usa OpenPDF)
    private final ReportGateway reportGateway;

    public ReservationService(
            ReservationRepository reservationRepository,
            LaboratoryRepository laboratoryRepository,
            UserRepository userRepository,
            Clock clock,
            ReportGateway reportGateway
    ) {
        this.reservationRepository = reservationRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.userRepository = userRepository;
        this.clock = clock;
        this.reportGateway = reportGateway;
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> findById(UUID reservationId) {
        if (reservationId == null) {
            throw new IllegalArgumentException("El id de la reserva no puede ser nulo");
        }
        return reservationRepository.findById(reservationId);
    }

    public List<Reservation> findByUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("El id del usuario no puede ser nulo");
        }
        return reservationRepository.findAll().stream()
                .filter(reservation -> reservation.userId().equals(userId))
                .toList();
    }

    public Reservation create(UUID laboratoryId, UUID userId, ReservationDTO reservationRequest) {
        if (laboratoryId == null) {
            throw new IllegalArgumentException("El id del laboratorio no puede ser nulo");
        }
        if (userId == null) {
            throw new IllegalArgumentException("El id del usuario no puede ser nulo");
        }
        if (reservationRequest == null) {
            throw new IllegalArgumentException("La solicitud de reserva no puede ser nula");
        }
        // Se validan aquí (con el mismo mensaje de la entidad) porque las reglas de abajo los usan
        // antes de que se construya la entidad
        if (reservationRequest.date() == null) {
            throw new IllegalArgumentException("La fecha de la reserva no puede ser nula");
        }
        if (reservationRequest.startTime() == null) {
            throw new IllegalArgumentException("La hora de inicio de la reserva no puede ser nula");
        }
        if (reservationRequest.endTime() == null) {
            throw new IllegalArgumentException("La hora de fin de la reserva no puede ser nula");
        }

        Laboratory laboratory = laboratoryRepository.findById(laboratoryId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el laboratorio asociado a la reserva"));
        if (userRepository.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("No existe el usuario asociado a la reserva");
        }
        if (laboratory.state() != LaboratoryState.LSTATE_ACTIVE) {
            throw new IllegalArgumentException("El laboratorio no está disponible para reservas");
        }
        // Se permite llenar el laboratorio exactamente hasta su capacidad
        if (reservationRequest.attendees() > laboratory.capacity()) {
            throw new IllegalArgumentException("El número de asistentes supera la capacidad del laboratorio");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (reservationRequest.date().isBefore(now.toLocalDate())) {
            throw new IllegalArgumentException("No se puede reservar en una fecha pasada");
        }
        if (reservationRequest.date().isEqual(now.toLocalDate())
                && reservationRequest.startTime().isBefore(now.toLocalTime())) {
            throw new IllegalArgumentException("No se puede reservar en una hora que ya pasó");
        }

        // Se valida antes del cruce (con el mismo mensaje de la entidad): con un rango invertido
        // la fórmula del cruce puede dar verdadero y el usuario recibiría un mensaje engañoso
        if (!reservationRequest.endTime().isAfter(reservationRequest.startTime())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // Cruce: nuevaInicio < existenteFin && nuevaFin > existenteInicio
        // (10:00-12:00 y 12:00-14:00 no se cruzan). Solo cuentan las reservas pendientes o aprobadas.
        boolean overlaps = reservationRepository.findAll().stream()
                .filter(reservation -> reservation.laboratoryId().equals(laboratoryId))
                .filter(reservation -> reservation.date().isEqual(reservationRequest.date()))
                .filter(reservation -> reservation.state() == ReservationState.RSTATE_PENDING
                        || reservation.state() == ReservationState.RSTATE_APPROVED)
                .anyMatch(reservation -> reservationRequest.startTime().isBefore(reservation.endTime())
                        && reservationRequest.endTime().isAfter(reservation.startTime()));
        if (overlaps) {
            throw new IllegalArgumentException("El laboratorio ya tiene una reserva en ese horario");
        }

        // Toda reserva nueva queda pendiente, sin importar el estado que traiga la solicitud
        ReservationDTO pendingRequest = new ReservationDTO(
                reservationRequest.date(),
                reservationRequest.startTime(),
                reservationRequest.endTime(),
                reservationRequest.attendees(),
                reservationRequest.purpose(),
                ReservationState.RSTATE_PENDING
        );
        return reservationRepository.create(laboratoryId, userId, pendingRequest);
    }

    public Reservation approve(UUID reservationId) {
        Reservation reservation = findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la reserva"));
        if (reservation.state() != ReservationState.RSTATE_PENDING) {
            throw new IllegalArgumentException("Solo se puede aprobar una reserva pendiente");
        }
        return changeState(reservation, ReservationState.RSTATE_APPROVED);
    }

    public Reservation reject(UUID reservationId) {
        Reservation reservation = findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la reserva"));
        if (reservation.state() != ReservationState.RSTATE_PENDING) {
            throw new IllegalArgumentException("Solo se puede rechazar una reserva pendiente");
        }
        return changeState(reservation, ReservationState.RSTATE_REJECTED);
    }

    public Reservation cancel(UUID reservationId, UUID requestedByUserId) {
        if (requestedByUserId == null) {
            throw new IllegalArgumentException("El id del usuario que solicita la cancelación no puede ser nulo");
        }
        Reservation reservation = findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la reserva"));
        User requester = userRepository.findById(requestedByUserId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el usuario que solicita la cancelación"));
        // Un usuario solo cancela sus propias reservas; un administrador cancela cualquiera
        boolean isOwner = reservation.userId().equals(requester.userId());
        boolean isAdmin = requester.role() == UserRole.ROLE_ADMIN;
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Solo el dueño de la reserva o un administrador puede cancelarla");
        }
        if (reservation.state() != ReservationState.RSTATE_PENDING
                && reservation.state() != ReservationState.RSTATE_APPROVED) {
            throw new IllegalArgumentException("Solo se puede cancelar una reserva pendiente o aprobada");
        }
        return changeState(reservation, ReservationState.RSTATE_CANCELLED);
    }

    public Reservation complete(UUID reservationId) {
        Reservation reservation = findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la reserva"));
        if (reservation.state() != ReservationState.RSTATE_APPROVED) {
            throw new IllegalArgumentException("Solo se puede completar una reserva aprobada");
        }
        return changeState(reservation, ReservationState.RSTATE_COMPLETED);
    }

    // Reporte de las reservas (en cualquier estado) entre dos fechas, ambas incluidas, ordenadas por fecha y hora
    public byte[] generateReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("La fecha inicial y la fecha final son obligatorias");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final");
        }
        List<Reservation> reservations = reservationRepository.findAll().stream()
                .filter(reservation -> !reservation.date().isBefore(startDate)
                        && !reservation.date().isAfter(endDate))
                .sorted(Comparator.comparing(Reservation::date).thenComparing(Reservation::startTime))
                .toList();
        return reportGateway.generateReservationReport(
                startDate,
                endDate,
                reservations,
                laboratoryRepository.findAll(),
                userRepository.findAll(),
                LocalDateTime.now(clock)
        );
    }

    // Guarda la misma reserva con un nuevo estado (el puerto solo tiene update para cambiarla)
    private Reservation changeState(Reservation reservation, ReservationState newState) {
        ReservationDTO request = new ReservationDTO(
                reservation.date(),
                reservation.startTime(),
                reservation.endTime(),
                reservation.attendees(),
                reservation.purpose(),
                newState
        );
        return reservationRepository.update(reservation.reservationId(), request)
                .orElseThrow(() -> new IllegalArgumentException("No existe la reserva"));
    }
}
