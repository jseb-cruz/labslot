package dev.team.labslot.adapter.repository.mariadb.entity;

import dev.team.labslot.core.constant.ReservationState;
import dev.team.labslot.core.entity.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Fila de la tabla reservations. Los ids van como String porque las columnas son VARCHAR(36).
 * La fecha se llama reservationDate porque DATE es palabra clave en SQL; en el dominio se llama date.
 */
public record ReservationJdbi(
        String reservationId,
        String laboratoryId,
        String userId,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime,
        int attendees,
        String purpose,
        ReservationState state,
        LocalDateTime createdAt
) {
    public static ReservationJdbi fromDomain(Reservation reservation) {
        return new ReservationJdbi(
                reservation.reservationId().toString(),
                reservation.laboratoryId().toString(),
                reservation.userId().toString(),
                reservation.date(),
                reservation.startTime(),
                reservation.endTime(),
                reservation.attendees(),
                reservation.purpose(),
                reservation.state(),
                reservation.createdAt()
        );
    }

    public Reservation toDomain() {
        return new Reservation(
                UUID.fromString(reservationId),
                UUID.fromString(laboratoryId),
                UUID.fromString(userId),
                reservationDate,
                startTime,
                endTime,
                attendees,
                purpose,
                state,
                createdAt
        );
    }
}
