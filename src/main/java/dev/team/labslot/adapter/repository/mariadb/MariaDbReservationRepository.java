package dev.team.labslot.adapter.repository.mariadb;

import dev.team.labslot.adapter.repository.mariadb.entity.ReservationJdbi;
import dev.team.labslot.adapter.repository.mariadb.repositories.ReservationDao;
import dev.team.labslot.core.dto.ReservationDTO;
import dev.team.labslot.core.entity.Reservation;
import dev.team.labslot.core.port.ReservationRepository;
import org.jdbi.v3.core.Jdbi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de reservas con JDBI. Se crea con @Bean en MariaDbConfig.
 */
public class MariaDbReservationRepository implements ReservationRepository {

    private final ReservationDao reservationDao;

    public MariaDbReservationRepository(Jdbi jdbi) {
        // onDemand abre y cierra una conexión en cada llamada
        this.reservationDao = jdbi.onDemand(ReservationDao.class);
    }

    // Devuelve todas las reservas
    @Override
    public List<Reservation> findAll() {
        return reservationDao.findAll()
                .stream()
                .map(ReservationJdbi::toDomain)
                .toList();
    }

    // Busca una reserva por su id
    @Override
    public Optional<Reservation> findById(UUID reservationId) {
        return reservationDao.findById(reservationId.toString())
                .map(ReservationJdbi::toDomain);
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
        reservationDao.insert(ReservationJdbi.fromDomain(reservation));
        return reservation;
    }

    // Actualiza una reserva existente; conserva su id, laboratorio, usuario y fecha de creación
    @Override
    public Optional<Reservation> update(UUID reservationId, ReservationDTO reservationRequest) {
        return findById(reservationId)
                .map(existing -> {
                    Reservation updatedReservation = new Reservation(
                            existing.reservationId(),
                            existing.laboratoryId(),
                            existing.userId(),
                            reservationRequest.date(),
                            reservationRequest.startTime(),
                            reservationRequest.endTime(),
                            reservationRequest.attendees(),
                            reservationRequest.purpose(),
                            reservationRequest.state(),
                            existing.createdAt()
                    );
                    reservationDao.update(ReservationJdbi.fromDomain(updatedReservation));
                    return updatedReservation;
                });
    }

    // Elimina una reserva por su id
    @Override
    public void delete(UUID reservationId) {
        reservationDao.delete(reservationId.toString());
    }
}
