package dev.team.labslot.core.entity;

import dev.team.labslot.core.constant.Constants;
import dev.team.labslot.core.constant.UserRole;

import java.util.UUID;

public record User(
        UUID userId,
        String fullName,
        String email,
        String password,
        UserRole role
) {
    // Formato básico de email: texto@texto.dominio, sin espacios y con una sola @
    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    public User(UUID userId, String fullName, String email, String password, UserRole role) {
        if (userId == null) {
            throw new IllegalArgumentException("El id del usuario no puede ser nulo");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("El nombre completo del usuario no puede ser nulo o vacío");
        }
        if (fullName.length() > Constants.MAX_FULL_NAME_LENGTH) {
            throw new IllegalArgumentException("El nombre completo del usuario no puede tener más de "
                    + Constants.MAX_FULL_NAME_LENGTH + " caracteres");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email del usuario no puede ser nulo o vacío");
        }
        if (email.length() > Constants.MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("El email del usuario no puede tener más de "
                    + Constants.MAX_EMAIL_LENGTH + " caracteres");
        }
        if (!email.matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("El email del usuario no tiene un formato válido");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña del usuario no puede ser nula o vacía");
        }
        if (role == null) {
            throw new IllegalArgumentException("El rol del usuario no puede ser nulo");
        }
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
