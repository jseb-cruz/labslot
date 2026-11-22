package dev.team.labslot.core.dto;

import dev.team.labslot.core.constant.UserRole;

/**
 * DTO con los datos de un usuario (sin id).
 * La contraseña viene en las solicitudes, pero nunca debe devolverse en las respuestas.
 */
public record UserDTO(
        String fullName,
        String email,
        String password,
        UserRole role
) {
}
