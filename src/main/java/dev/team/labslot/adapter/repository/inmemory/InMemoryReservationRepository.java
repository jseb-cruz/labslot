package dev.team.labslot.adapter.repository.inmemory;

import dev.team.labslot.core.dto.ReservationDTO;
import dev.team.labslot.core.entity.Reservation;
import dev.team.labslot.core.port.ReservationRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de reservas en memoria. Guarda las reservas en una lista interna.
 */
public class InMemoryReservationRepository implements ReservationRepository {

    // Lista interna de almacenamiento
    private final ArrayList<Reservation> reservations = new ArrayList<>();

    // Devuelve una copia inmutable para que nadie modifique la lista interna desde afuera
    @Override
    public List<Reservation> findAll() {
        return List.copyOf(reservations);
    }

    // Busca una reserva por su id
    @Override
    public Optional<Reservation> findById(UUID reservationId) {
        return reservations.stream()
                .filter(reservation -> reservation.reservationId().equals(reservationId))
                .findFirst();
    }

    // Crea una reserva asociada a un laboratorio y a un usuario; genera el id y la fecha de creación
    @Override
    public Reservation create(UUID laboratoryId, UUID userId, ReservationDTO reservationRequest) {
        Reservation reservation = new Reservation(
                UUID.randomUUID(),
                laboratoryId,
                userId,
                reservationRequest.date(),
                reservationRequest.startTime(),
                reservationRequest.endTime(),
                reservationRequest.attendees(),
                reservationRequest.purpose(),
                reservationRequest.state(),
                LocalDateTime.now()
        );
        reservations.add(reservation);
        return reservation;
    }

    // Actualiza una reserva existente; conserva su id, laboratorio, usuario y fecha de creación
    @Override
    public Optional<Reservation> update(UUID reservationId, ReservationDTO reservationRequest) {
        for (Reservation reservation : reservations) {
            if (reservation.reservationId().equals(reservationId)) {
                Reservation updatedReservation = new Reservation(
                        reservation.reservationId(),
                        reservation.laboratoryId(),
                        reservation.userId(),
                        reservationRequest.date(),
                        reservationRequest.startTime(),
                        reservationRequest.endTime(),
                        reservationRequest.attendees(),
                        reservationRequest.purpose(),
                        reservationRequest.state(),
                        reservation.createdAt()
                );
                reservations.set(reservations.indexOf(reservation), updatedReservation);
                return Optional.of(updatedReservation);
            }
        }
        return Optional.empty();
    }

    // Elimina una reserva por su id
    @Override
    public void delete(UUID reservationId) {
        reservations.removeIf(r -> r.reservationId().equals(reservationId));
    }
}
