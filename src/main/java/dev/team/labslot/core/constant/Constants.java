package dev.team.labslot.core.constant;

import java.time.LocalTime;

public class Constants {
    // Horario en el que se pueden reservar los laboratorios (ambos extremos permitidos)
    public static final LocalTime OPENING_TIME = LocalTime.of(6, 0);
    public static final LocalTime CLOSING_TIME = LocalTime.of(22, 0);
    // Duración máxima de una reserva (4 horas exactas permitidas)
    public static final int MAX_RESERVATION_HOURS = 4;
    // Longitudes máximas de los textos (las mismas de las columnas VARCHAR de schema.sql)
    public static final int MAX_LABORATORY_CODE_LENGTH = 20;
    public static final int MAX_LABORATORY_NAME_LENGTH = 150;
    public static final int MAX_BUILDING_LENGTH = 150;
    public static final int MAX_PURPOSE_LENGTH = 255;
    public static final int MAX_FULL_NAME_LENGTH = 150;
    public static final int MAX_EMAIL_LENGTH = 150;
    // Longitud de la contraseña. El máximo va en bytes porque es el límite de BCrypt (una tilde o una ñ ocupan 2)
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_BYTES = 72;
}
