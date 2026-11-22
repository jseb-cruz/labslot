package dev.team.labslot.core.service;

import dev.team.labslot.adapter.config.InMemoryConfig;
import dev.team.labslot.core.constant.*;
import dev.team.labslot.core.dto.*;
import dev.team.labslot.core.entity.*;
import dev.team.labslot.core.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServiceBusinessRulesTest {

    // "Ahora" fijo para los tests: 17 de noviembre de 2026 a las 10:00 en Bogotá
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 17);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDateTime.of(TODAY, LocalTime.of(10, 0)).atZone(BOGOTA).toInstant(), BOGOTA);

    private LaboratoryService laboratoryService;
    private UserService userService;
    private ReservationService reservationService;
    private LaboratoryRepository laboratoryRepository;
    private ReservationRepository reservationRepository;
    private UserRepository userRepository;

    // Lo que recibe el gateway falso del reporte (sin Mockito: una lambda que guarda los argumentos)
    private List<Reservation> reportedReservations;
    private LocalDateTime reportedGeneratedAt;

    private Laboratory laboratory;
    private User owner;
    private User otherUser;
    private User admin;

    @BeforeEach
    void setUp() {
        InMemoryConfig config = new InMemoryConfig();
        laboratoryRepository = config.laboratoryRepository();
        reservationRepository = config.reservationRepository();
        userRepository = config.userRepository();

        laboratoryService = config.laboratoryService(laboratoryRepository, reservationRepository);
        userService = config.userService(userRepository, reservationRepository);
        // Se pasa un reloj fijo en vez de config.clock() para que el test no dependa de la hora real,
        // y un gateway falso en vez del PDF real para revisar qué datos le llegan al reporte
        ReportGateway fakeReportGateway = (startDate, endDate, reservations, laboratories, users, generatedAt) -> {
            reportedReservations = reservations;
            reportedGeneratedAt = generatedAt;
            return new byte[]{1};
        };
        reservationService = config.reservationService(reservationRepository, laboratoryRepository, userRepository, FIXED_CLOCK, fakeReportGateway);

        laboratory = laboratoryRepository.create(new LaboratoryDTO("LAB-101", "Laboratorio de Redes", "Edificio Santo Domingo", 20, LaboratoryState.LSTATE_ACTIVE));
        owner = userRepository.create(new UserDTO("Ana Pérez", "ana@usantoto.edu.co", "1234", UserRole.ROLE_USER));
        otherUser = userRepository.create(new UserDTO("Luis Gómez", "luis@usantoto.edu.co", "1234", UserRole.ROLE_USER));
        admin = userRepository.create(new UserDTO("Admin LabSlot", "admin@usantoto.edu.co", "1234", UserRole.ROLE_ADMIN));
    }

    // Solicitud de reserva para mañana con 10 asistentes
    private ReservationDTO requestTomorrow(int startHour, int endHour) {
        return new ReservationDTO(TOMORROW, LocalTime.of(startHour, 0), LocalTime.of(endHour, 0), 10, "Clase de redes", ReservationState.RSTATE_PENDING);
    }

    // ---------- Laboratorios ----------

    @Test
    void shouldRejectDuplicateLaboratoryCode() {
        LaboratoryDTO dto = new LaboratoryDTO("LAB-101", "Otro laboratorio", "Edificio Central", 10, LaboratoryState.LSTATE_ACTIVE);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> laboratoryService.create(dto));

        assertEquals("Ya existe un laboratorio con ese código", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicateLaboratoryCodeIgnoringCase() {
        LaboratoryDTO dto = new LaboratoryDTO("lab-101", "Otro laboratorio", "Edificio Central", 10, LaboratoryState.LSTATE_ACTIVE);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> laboratoryService.create(dto));

        assertEquals("Ya existe un laboratorio con ese código", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicateLaboratoryCodeOnUpdate() {
        Laboratory second = laboratoryService.create(new LaboratoryDTO("LAB-202", "Laboratorio de Física", "Edificio Central", 15, LaboratoryState.LSTATE_ACTIVE));
        LaboratoryDTO dto = new LaboratoryDTO("LAB-101", "Laboratorio de Física", "Edificio Central", 15, LaboratoryState.LSTATE_ACTIVE);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> laboratoryService.update(second.laboratoryId(), dto));

        assertEquals("Ya existe otro laboratorio con ese código", exception.getMessage());
    }

    @Test
    void shouldUpdateLaboratoryKeepingItsOwnCode() {
        LaboratoryDTO dto = new LaboratoryDTO("LAB-101", "Laboratorio de Redes", "Edificio Santo Domingo", 25, LaboratoryState.LSTATE_MAINTENANCE);

        Laboratory updated = laboratoryService.update(laboratory.laboratoryId(), dto).orElseThrow();

        assertEquals(25, updated.capacity());
        assertEquals(LaboratoryState.LSTATE_MAINTENANCE, updated.state());
    }

    @Test
    void shouldRejectDeleteLaboratoryWithReservations() {
        reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> laboratoryService.delete(laboratory.laboratoryId()));

        assertEquals("No se puede eliminar un laboratorio con reservas", exception.getMessage());
    }

    @Test
    void shouldDeleteLaboratoryWithoutReservations() {
        laboratoryService.delete(laboratory.laboratoryId());

        assertTrue(laboratoryService.findById(laboratory.laboratoryId()).isEmpty());
    }

    // ---------- Usuarios ----------

    @Test
    void shouldRejectDuplicateUserEmail() {
        UserDTO dto = new UserDTO("Ana María Pérez", "ana@usantoto.edu.co", "1234", UserRole.ROLE_USER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.create(dto));

        assertEquals("Ya existe un usuario con ese email", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicateUserEmailIgnoringCase() {
        UserDTO dto = new UserDTO("Ana María Pérez", "ANA@usantoto.edu.co", "1234", UserRole.ROLE_USER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.create(dto));

        assertEquals("Ya existe un usuario con ese email", exception.getMessage());
    }

    @Test
    void shouldRejectDuplicateUserEmailOnUpdate() {
        UserDTO dto = new UserDTO("Luis Gómez", "ana@usantoto.edu.co", "1234", UserRole.ROLE_USER);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.update(otherUser.userId(), dto));

        assertEquals("Ya existe otro usuario con ese email", exception.getMessage());
    }

    @Test
    void shouldFindUserByEmailIgnoringCase() {
        User found = userService.findByEmail("ANA@usantoto.edu.co").orElseThrow();

        assertEquals(owner.userId(), found.userId());
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        assertTrue(userService.findByEmail("nadie@usantoto.edu.co").isEmpty());
    }

    @Test
    void shouldRejectDeleteUserWithReservations() {
        reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.delete(owner.userId()));

        assertEquals("No se puede eliminar un usuario con reservas", exception.getMessage());
    }

    @Test
    void shouldDeleteUserWithoutReservations() {
        userService.delete(otherUser.userId());

        assertTrue(userService.findById(otherUser.userId()).isEmpty());
    }

    // ---------- Reservas: existencia y datos ----------

    @Test
    void shouldRejectReservationForUnknownLaboratory() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.create(UUID.randomUUID(), owner.userId(), requestTomorrow(10, 12)));

        assertEquals("No existe el laboratorio asociado a la reserva", exception.getMessage());
    }

    @Test
    void shouldRejectReservationForUnknownUser() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.create(laboratory.laboratoryId(), UUID.randomUUID(), requestTomorrow(10, 12)));

        assertEquals("No existe el usuario asociado a la reserva", exception.getMessage());
    }

    @Test
    void shouldRejectReservationWithNullDate() {
        ReservationDTO dto = new ReservationDTO(null, LocalTime.of(10, 0), LocalTime.of(12, 0), 10, "Clase de redes", ReservationState.RSTATE_PENDING);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.create(laboratory.laboratoryId(), owner.userId(), dto));

        assertEquals("La fecha de la reserva no puede ser nula", exception.getMessage());
    }

    @Test
    void shouldRejectReservationInLaboratoryUnderMaintenance() {
        Laboratory maintenanceLab = laboratoryRepository.create(new LaboratoryDTO("LAB-303", "Laboratorio de Química", "Edificio Central", 20, LaboratoryState.LSTATE_MAINTENANCE));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.create(maintenanceLab.laboratoryId(), owner.userId(), requestTomorrow(10, 12)));

        assertEquals("El laboratorio no está disponible para reservas", exception.getMessage());
    }

    @Test
    void shouldAcceptReservationWithAttendeesEqualToCapacity() {
        ReservationDTO dto = new ReservationDTO(TOMORROW, LocalTime.of(10, 0), LocalTime.of(12, 0), 20, "Clase de redes", ReservationState.RSTATE_PENDING);

        Reservation reservation = reservationService.create(laboratory.laboratoryId(), owner.userId(), dto);

        assertEquals(20, reservation.attendees());
    }

    @Test
    void shouldRejectReservationWithAttendeesOverCapacity() {
        ReservationDTO dto = new ReservationDTO(TOMORROW, LocalTime.of(10, 0), LocalTime.of(12, 0), 21, "Clase de redes", ReservationState.RSTATE_PENDING);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.create(laboratory.laboratoryId(), owner.userId(), dto));

        assertEquals("El número de asistentes supera la capacidad del laboratorio", exception.getMessage());
    }

    // ---------- Reservas: fecha y hora ----------

    @Test
    void shouldRejectReservationOnPastDate() {
        ReservationDTO dto = new ReservationDTO(TODAY.minusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0), 10, "Clase de redes", ReservationState.RSTATE_PENDING);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.create(laboratory.laboratoryId(), owner.userId(), dto));

        assertEquals("No se puede reservar en una fecha pasada", exception.getMessage());
    }

    @Test
    void shouldRejectReservationTodayAtPastTime() {
        // El reloj marca las 10:00, así que las 09:00 de hoy ya pasaron
        ReservationDTO dto = new ReservationDTO(TODAY, LocalTime.of(9, 0), LocalTime.of(11, 0), 10, "Clase de redes", ReservationState.RSTATE_PENDING);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.create(laboratory.laboratoryId(), owner.userId(), dto));

        assertEquals("No se puede reservar en una hora que ya pasó", exception.getMessage());
    }

    @Test
    void shouldAcceptReservationTodayAtFutureTime() {
        ReservationDTO dto = new ReservationDTO(TODAY, LocalTime.of(11, 0), LocalTime.of(13, 0), 10, "Clase de redes", ReservationState.RSTATE_PENDING);

        Reservation reservation = reservationService.create(laboratory.laboratoryId(), owner.userId(), dto);

        assertEquals(TODAY, reservation.date());
    }

    @Test
    void shouldCreateReservationInPendingState() {
        // Aunque la solicitud diga APPROVED, la reserva nueva debe quedar pendiente
        ReservationDTO dto = new ReservationDTO(TOMORROW, LocalTime.of(10, 0), LocalTime.of(12, 0), 10, "Clase de redes", ReservationState.RSTATE_APPROVED);

        Reservation reservation = reservationService.create(laboratory.laboratoryId(), owner.userId(), dto);

        assertEquals(ReservationState.RSTATE_PENDING, reservation.state());
    }

    // ---------- Reservas: cruces de horario ----------

    @Test
    void shouldRejectOverlapWithPendingReservation() {
        reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.create(laboratory.laboratoryId(), otherUser.userId(), requestTomorrow(11, 13)));

        assertEquals("El laboratorio ya tiene una reserva en ese horario", exception.getMessage());
    }

    @Test
    void shouldRejectOverlapWithApprovedReservation() {
        Reservation existing = reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));
        reservationService.approve(existing.reservationId());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.create(laboratory.laboratoryId(), otherUser.userId(), requestTomorrow(9, 11)));

        assertEquals("El laboratorio ya tiene una reserva en ese horario", exception.getMessage());
    }

    @Test
    void shouldAcceptOverlapWithRejectedReservation() {
        Reservation existing = reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));
        reservationService.reject(existing.reservationId());

        Reservation reservation = reservationService.create(laboratory.laboratoryId(), otherUser.userId(), requestTomorrow(10, 12));

        assertEquals(ReservationState.RSTATE_PENDING, reservation.state());
    }

    @Test
    void shouldAcceptOverlapWithCancelledReservation() {
        Reservation existing = reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));
        reservationService.cancel(existing.reservationId(), owner.userId());

        Reservation reservation = reservationService.create(laboratory.laboratoryId(), otherUser.userId(), requestTomorrow(10, 12));

        assertEquals(ReservationState.RSTATE_PENDING, reservation.state());
    }

    @Test
    void shouldAcceptContiguousReservations() {
        reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));

        Reservation reservation = reservationService.create(laboratory.laboratoryId(), otherUser.userId(), requestTomorrow(12, 14));

        assertEquals(LocalTime.of(12, 0), reservation.startTime());
    }

    @Test
    void shouldRejectInvertedRangeBeforeCheckingOverlap() {
        // 13:00-11:00 con una reserva de 10:00-12:00: la fórmula del cruce daría verdadero,
        // pero el mensaje correcto es el del rango invertido
        reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.create(laboratory.laboratoryId(), otherUser.userId(), requestTomorrow(13, 11)));

        assertEquals("La hora de fin debe ser posterior a la hora de inicio", exception.getMessage());
    }

    @Test
    void shouldAcceptSameTimeInAnotherLaboratory() {
        Laboratory anotherLab = laboratoryRepository.create(new LaboratoryDTO("LAB-202", "Laboratorio de Física", "Edificio Central", 20, LaboratoryState.LSTATE_ACTIVE));
        reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));

        Reservation reservation = reservationService.create(anotherLab.laboratoryId(), otherUser.userId(), requestTomorrow(10, 12));

        assertEquals(anotherLab.laboratoryId(), reservation.laboratoryId());
    }

    // ---------- Reservas: transiciones ----------

    @Test
    void shouldApprovePendingReservation() {
        Reservation reservation = reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));

        Reservation approved = reservationService.approve(reservation.reservationId());

        assertEquals(ReservationState.RSTATE_APPROVED, approved.state());
    }

    @Test
    void shouldRejectApproveWhenNotPending() {
        Reservation reservation = reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));
        reservationService.approve(reservation.reservationId());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.approve(reservation.reservationId()));

        assertEquals("Solo se puede aprobar una reserva pendiente", exception.getMessage());
    }

    @Test
    void shouldRejectPendingReservation() {
        Reservation reservation = reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));

        Reservation rejected = reservationService.reject(reservation.reservationId());

        assertEquals(ReservationState.RSTATE_REJECTED, rejected.state());
    }

    @Test
    void shouldRejectRejectWhenNotPending() {
        Reservation reservation = reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));
        reservationService.approve(reservation.reservationId());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.reject(reservation.reservationId()));

        assertEquals("Solo se puede rechazar una reserva pendiente", exception.getMessage());
    }

    @Test
    void shouldCompleteApprovedReservation() {
        Reservation reservation = reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));
        reservationService.approve(reservation.reservationId());

        Reservation completed = reservationService.complete(reservation.reservationId());

        assertEquals(ReservationState.RSTATE_COMPLETED, completed.state());
    }

    @Test
    void shouldRejectCompleteWhenNotApproved() {
        Reservation reservation = reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.complete(reservation.reservationId()));

        assertEquals("Solo se puede completar una reserva aprobada", exception.getMessage());
    }

    @Test
    void shouldCancelApprovedReservation() {
        Reservation reservation = reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));
        reservationService.approve(reservation.reservationId());

        Reservation cancelled = reservationService.cancel(reservation.reservationId(), owner.userId());

        assertEquals(ReservationState.RSTATE_CANCELLED, cancelled.state());
    }

    @Test
    void shouldRejectCancelWhenNotPendingOrApproved() {
        Reservation reservation = reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));
        reservationService.reject(reservation.reservationId());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.cancel(reservation.reservationId(), owner.userId()));

        assertEquals("Solo se puede cancelar una reserva pendiente o aprobada", exception.getMessage());
    }

    @Test
    void shouldRejectTransitionForUnknownReservation() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.approve(UUID.randomUUID()));

        assertEquals("No existe la reserva", exception.getMessage());
    }

    // ---------- Reservas: quién puede cancelar ----------

    @Test
    void shouldAllowOwnerToCancelPendingReservation() {
        Reservation reservation = reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));

        Reservation cancelled = reservationService.cancel(reservation.reservationId(), owner.userId());

        assertEquals(ReservationState.RSTATE_CANCELLED, cancelled.state());
    }

    @Test
    void shouldRejectCancelByAnotherUser() {
        Reservation reservation = reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.cancel(reservation.reservationId(), otherUser.userId()));

        assertEquals("Solo el dueño de la reserva o un administrador puede cancelarla", exception.getMessage());
    }

    @Test
    void shouldAllowAdminToCancelAnotherUsersReservation() {
        Reservation reservation = reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));

        Reservation cancelled = reservationService.cancel(reservation.reservationId(), admin.userId());

        assertEquals(ReservationState.RSTATE_CANCELLED, cancelled.state());
    }

    // ---------- Reporte ----------

    @Test
    void shouldRejectReportWhenStartDateIsAfterEndDate() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.generateReport(TOMORROW, TODAY));

        assertEquals("La fecha inicial no puede ser posterior a la fecha final", exception.getMessage());
    }

    @Test
    void shouldRejectReportWithNullDates() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.generateReport(null, TODAY));

        assertEquals("La fecha inicial y la fecha final son obligatorias", exception.getMessage());
    }

    @Test
    void shouldGenerateReportForASingleDay() {
        reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));

        byte[] report = reservationService.generateReport(TOMORROW, TOMORROW);

        assertArrayEquals(new byte[]{1}, report);
        assertEquals(1, reportedReservations.size());
        assertEquals(LocalDateTime.of(TODAY, LocalTime.of(10, 0)), reportedGeneratedAt);
    }

    @Test
    void shouldReportOnlyReservationsInRangeSortedByDateAndTime() {
        reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(12, 14));
        reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(8, 10));
        reservationService.create(laboratory.laboratoryId(), owner.userId(), new ReservationDTO(
                TOMORROW.plusDays(2), LocalTime.of(9, 0), LocalTime.of(11, 0), 10, "Dentro del rango", ReservationState.RSTATE_PENDING));
        reservationService.create(laboratory.laboratoryId(), owner.userId(), new ReservationDTO(
                TOMORROW.plusDays(10), LocalTime.of(9, 0), LocalTime.of(11, 0), 10, "Fuera del rango", ReservationState.RSTATE_PENDING));

        reservationService.generateReport(TOMORROW, TOMORROW.plusDays(2));

        assertEquals(3, reportedReservations.size());
        assertEquals(LocalTime.of(8, 0), reportedReservations.get(0).startTime());
        assertEquals(LocalTime.of(12, 0), reportedReservations.get(1).startTime());
        assertEquals(TOMORROW.plusDays(2), reportedReservations.get(2).date());
    }

    // ---------- Reservas: consultas ----------

    @Test
    void shouldFindReservationsByUser() {
        reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(8, 10));
        reservationService.create(laboratory.laboratoryId(), owner.userId(), requestTomorrow(10, 12));
        reservationService.create(laboratory.laboratoryId(), otherUser.userId(), requestTomorrow(12, 14));

        List<Reservation> ownerReservations = reservationService.findByUser(owner.userId());

        assertEquals(2, ownerReservations.size());
        assertTrue(ownerReservations.stream().allMatch(reservation -> reservation.userId().equals(owner.userId())));
    }
}
