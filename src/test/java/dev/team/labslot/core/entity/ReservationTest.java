package dev.team.labslot.core.entity;

import dev.team.labslot.core.constant.ReservationState;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReservationTest {

    private final UUID mockId = UUID.randomUUID();
    private final UUID mockLaboratoryId = UUID.randomUUID();
    private final UUID mockUserId = UUID.randomUUID();
    private final LocalDate mockDate = LocalDate.of(2026, 12, 1);
    private final ReservationState mockState = ReservationState.RSTATE_PENDING;
    private final LocalDateTime mockCreatedAt = LocalDateTime.of(2026, 11, 17, 9, 0);

    @Test
    void ShouldCreateReservationWithValidData() {
        Reservation reservation = new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt);
        assertEquals(mockId, reservation.reservationId());
        assertEquals(mockLaboratoryId, reservation.laboratoryId());
        assertEquals(mockUserId, reservation.userId());
        assertEquals(mockDate, reservation.date());
        assertEquals(LocalTime.of(10, 0), reservation.startTime());
        assertEquals(LocalTime.of(12, 0), reservation.endTime());
        assertEquals(20, reservation.attendees());
        assertEquals("Clase de redes", reservation.purpose());
        assertEquals(mockState, reservation.state());
        assertEquals(mockCreatedAt, reservation.createdAt());
    }

    // ---------- Campos obligatorios ----------

    @Test
    void ShouldRejectNullId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                null,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt));
        assertEquals("El id de la reserva no puede ser nulo", exception.getMessage());
    }

    @Test
    void ShouldRejectNullLaboratoryId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                null,
                mockUserId,
                mockDate,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt));
        assertEquals("El id del laboratorio no puede ser nulo", exception.getMessage());
    }

    @Test
    void ShouldRejectNullUserId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                null,
                mockDate,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt));
        assertEquals("El id del usuario no puede ser nulo", exception.getMessage());
    }

    @Test
    void ShouldRejectNullDate() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                null,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt));
        assertEquals("La fecha de la reserva no puede ser nula", exception.getMessage());
    }

    @Test
    void ShouldRejectNullStartTime() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                null,
                LocalTime.of(12, 0),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt));
        assertEquals("La hora de inicio de la reserva no puede ser nula", exception.getMessage());
    }

    @Test
    void ShouldRejectNullEndTime() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(10, 0),
                null,
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt));
        assertEquals("La hora de fin de la reserva no puede ser nula", exception.getMessage());
    }

    @Test
    void ShouldRejectZeroAttendees() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                0,
                "Clase de redes",
                mockState,
                mockCreatedAt));
        assertEquals("El número de asistentes debe ser mayor que cero", exception.getMessage());
    }

    @Test
    void ShouldRejectNegativeAttendees() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                -3,
                "Clase de redes",
                mockState,
                mockCreatedAt));
        assertEquals("El número de asistentes debe ser mayor que cero", exception.getMessage());
    }

    @Test
    void ShouldRejectNullPurpose() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                20,
                null,
                mockState,
                mockCreatedAt));
        assertEquals("El propósito de la reserva no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void ShouldRejectBlankPurpose() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                20,
                "   ",
                mockState,
                mockCreatedAt));
        assertEquals("El propósito de la reserva no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void ShouldRejectNullState() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                20,
                "Clase de redes",
                null,
                mockCreatedAt));
        assertEquals("El estado de la reserva no puede ser nulo", exception.getMessage());
    }

    @Test
    void ShouldRejectNullCreatedAt() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                20,
                "Clase de redes",
                mockState,
                null));
        assertEquals("La fecha de creación de la reserva no puede ser nula", exception.getMessage());
    }

    // ---------- Hora de fin posterior a la de inicio ----------

    @Test
    void ShouldRejectEndTimeBeforeStartTime() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(12, 0),
                LocalTime.of(10, 0),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt));
        assertEquals("La hora de fin debe ser posterior a la hora de inicio", exception.getMessage());
    }

    @Test
    void ShouldRejectEndTimeEqualToStartTime() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(10, 0),
                LocalTime.of(10, 0),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt));
        assertEquals("La hora de fin debe ser posterior a la hora de inicio", exception.getMessage());
    }

    // ---------- Horario 06:00 - 22:00 (casos límite) ----------

    @Test
    void ShouldCreateReservationStartingAtOpeningTime() {
        Reservation reservation = new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(6, 0),
                LocalTime.of(8, 0),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt);
        assertEquals(LocalTime.of(6, 0), reservation.startTime());
    }

    @Test
    void ShouldRejectReservationStartingBeforeOpeningTime() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(5, 59),
                LocalTime.of(8, 0),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt));
        assertEquals("La reserva debe estar dentro del horario de 06:00 a 22:00", exception.getMessage());
    }

    @Test
    void ShouldCreateReservationEndingAtClosingTime() {
        Reservation reservation = new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(20, 0),
                LocalTime.of(22, 0),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt);
        assertEquals(LocalTime.of(22, 0), reservation.endTime());
    }

    @Test
    void ShouldRejectReservationEndingAfterClosingTime() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(20, 0),
                LocalTime.of(22, 1),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt));
        assertEquals("La reserva debe estar dentro del horario de 06:00 a 22:00", exception.getMessage());
    }

    // ---------- Duración máxima de 4 horas (casos límite) ----------

    @Test
    void ShouldCreateReservationWithExactlyFourHours() {
        Reservation reservation = new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt);
        assertEquals(LocalTime.of(14, 0), reservation.endTime());
    }

    @Test
    void ShouldCreateReservationWithOneMinuteLessThanFourHours() {
        Reservation reservation = new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(10, 0),
                LocalTime.of(13, 59),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt);
        assertEquals(LocalTime.of(13, 59), reservation.endTime());
    }

    @Test
    void ShouldRejectReservationLongerThanFourHours() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Reservation(
                mockId,
                mockLaboratoryId,
                mockUserId,
                mockDate,
                LocalTime.of(10, 0),
                LocalTime.of(14, 1),
                20,
                "Clase de redes",
                mockState,
                mockCreatedAt));
        assertEquals("La reserva no puede durar más de 4 horas", exception.getMessage());
    }
}
