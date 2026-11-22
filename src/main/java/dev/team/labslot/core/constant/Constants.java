package dev.team.labslot.core.constant;

import java.time.LocalTime;

public class Constants {
    // Horario en el que se pueden reservar los laboratorios (ambos extremos permitidos)
    public static final LocalTime OPENING_TIME = LocalTime.of(6, 0);
    public static final LocalTime CLOSING_TIME = LocalTime.of(22, 0);
    // Duración máxima de una reserva (4 horas exactas permitidas)
    public static final int MAX_RESERVATION_HOURS = 4;
}
