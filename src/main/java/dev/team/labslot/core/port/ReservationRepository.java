package dev.team.labslot.core.port;

import dev.team.labslot.core.dto.ReservationDTO;
import dev.team.labslot.core.entity.Reservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto con las operaciones de persistencia de reservas.
 */
public interface ReservationRepository {

    // Devuelve todas las reservas
    List<Reservation> findAll();

    // Busca una reserva por su id
    Optional<Reservation> findById(UUID reservationId);

    // Crea una reserva nueva asociada a un laboratorio y a un usuario
    Reservation create(UUID laboratoryId, UUID userId, ReservationDTO reservationRequest);

    // Actualiza una reserva existente; vacío si no existe
    Optional<Reservation> update(UUID reservationId, ReservationDTO reservationRequest);

    // Elimina una reserva por su id
    void delete(UUID reservationId);
}
