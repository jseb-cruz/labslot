package dev.team.labslot.core.entity;

import dev.team.labslot.core.constant.Constants;
import dev.team.labslot.core.constant.ReservationState;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record Reservation(
        UUID reservationId,
        UUID laboratoryId,
        UUID userId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        int attendees,
        String purpose,
        ReservationState state,
        LocalDateTime createdAt
) {
    public Reservation(UUID reservationId, UUID laboratoryId, UUID userId, LocalDate date, LocalTime startTime, LocalTime endTime, int attendees, String purpose, ReservationState state, LocalDateTime createdAt) {
        if (reservationId == null) {
            throw new IllegalArgumentException("El id de la reserva no puede ser nulo");
        }
        if (laboratoryId == null) {
            throw new IllegalArgumentException("El id del laboratorio no puede ser nulo");
        }
        if (userId == null) {
            throw new IllegalArgumentException("El id del usuario no puede ser nulo");
        }
        if (date == null) {
            throw new IllegalArgumentException("La fecha de la reserva no puede ser nula");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("La hora de inicio de la reserva no puede ser nula");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("La hora de fin de la reserva no puede ser nula");
        }
        if (attendees <= 0) {
            throw new IllegalArgumentException("El número de asistentes debe ser mayor que cero");
        }
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException("El propósito de la reserva no puede ser nulo o vacío");
        }
        if (purpose.length() > Constants.MAX_PURPOSE_LENGTH) {
            throw new IllegalArgumentException("El propósito de la reserva no puede tener más de "
                    + Constants.MAX_PURPOSE_LENGTH + " caracteres");
        }
        if (state == null) {
            throw new IllegalArgumentException("El estado de la reserva no puede ser nulo");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("La fecha de creación de la reserva no puede ser nula");
        }
        // La hora de fin debe ser estrictamente posterior a la de inicio (misma hora no es válida)
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }
        // La franja debe estar dentro del horario; 06:00 y 22:00 son extremos permitidos
        if (startTime.isBefore(Constants.OPENING_TIME) || endTime.isAfter(Constants.CLOSING_TIME)) {
            throw new IllegalArgumentException("La reserva debe estar dentro del horario de "
                    + Constants.OPENING_TIME + " a " + Constants.CLOSING_TIME);
        }
        // Se permite una duración de exactamente MAX_RESERVATION_HOURS horas, no más
        if (Duration.between(startTime, endTime).compareTo(Duration.ofHours(Constants.MAX_RESERVATION_HOURS)) > 0) {
            throw new IllegalArgumentException("La reserva no puede durar más de "
                    + Constants.MAX_RESERVATION_HOURS + " horas");
        }
        this.reservationId = reservationId;
        this.laboratoryId = laboratoryId;
        this.userId = userId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.attendees = attendees;
        this.purpose = purpose;
        this.state = state;
        this.createdAt = createdAt;
    }
}
