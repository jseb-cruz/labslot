package dev.team.labslot.core.dto;

import dev.team.labslot.core.constant.ReservationState;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO con los datos de una reserva.
 * No lleva el id ni los ids del laboratorio y del usuario (se pasan aparte),
 * ni la fecha de creación (la genera el repositorio).
 */
public record ReservationDTO(
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        int attendees,
        String purpose,
        ReservationState state
) {
}
