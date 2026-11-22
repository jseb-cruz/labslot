package dev.team.labslot.core.entity;

import dev.team.labslot.core.constant.UserRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserTest {

    private final UUID mockId = UUID.randomUUID();
    private final UserRole mockRole = UserRole.ROLE_USER;

    @Test
    void ShouldCreateUserWithValidData() {
        User user = new User(
                mockId,
                "Ana Pérez",
                "ana.perez@usantoto.edu.co",
                "password123",
                mockRole);
        assertEquals(mockId, user.userId());
        assertEquals("Ana Pérez", user.fullName());
        assertEquals("ana.perez@usantoto.edu.co", user.email());
        assertEquals("password123", user.password());
        assertEquals(mockRole, user.role());
    }

    @Test
    void ShouldRejectNullId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new User(
                null,
                "Ana Pérez",
                "ana.perez@usantoto.edu.co",
                "password123",
                mockRole));
        assertEquals("El id del usuario no puede ser nulo", exception.getMessage());
    }

    @Test
    void ShouldRejectNullFullName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new User(
                mockId,
                null,
                "ana.perez@usantoto.edu.co",
                "password123",
                mockRole));
        assertEquals("El nombre completo del usuario no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void ShouldRejectBlankFullName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new User(
                mockId,
                "   ",
                "ana.perez@usantoto.edu.co",
                "password123",
                mockRole));
        assertEquals("El nombre completo del usuario no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void ShouldRejectNullEmail() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new User(
                mockId,
                "Ana Pérez",
                null,
                "password123",
                mockRole));
        assertEquals("El email del usuario no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void ShouldRejectBlankEmail() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new User(
                mockId,
                "Ana Pérez",
                "   ",
                "password123",
                mockRole));
        assertEquals("El email del usuario no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void ShouldRejectEmailWithoutAt() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new User(
                mockId,
                "Ana Pérez",
                "ana.perez.usantoto.edu.co",
                "password123",
                mockRole));
        assertEquals("El email del usuario no tiene un formato válido", exception.getMessage());
    }

    @Test
    void ShouldRejectEmailWithoutDomain() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new User(
                mockId,
                "Ana Pérez",
                "ana.perez@usantoto",
                "password123",
                mockRole));
        assertEquals("El email del usuario no tiene un formato válido", exception.getMessage());
    }

    @Test
    void ShouldRejectEmailWithSpaces() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new User(
                mockId,
                "Ana Pérez",
                "ana perez@usantoto.edu.co",
                "password123",
                mockRole));
        assertEquals("El email del usuario no tiene un formato válido", exception.getMessage());
    }

    @Test
    void ShouldRejectNullPassword() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new User(
                mockId,
                "Ana Pérez",
                "ana.perez@usantoto.edu.co",
                null,
                mockRole));
        assertEquals("La contraseña del usuario no puede ser nula o vacía", exception.getMessage());
    }

    @Test
    void ShouldRejectBlankPassword() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new User(
                mockId,
                "Ana Pérez",
                "ana.perez@usantoto.edu.co",
                "   ",
                mockRole));
        assertEquals("La contraseña del usuario no puede ser nula o vacía", exception.getMessage());
    }

    @Test
    void ShouldRejectNullRole() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new User(
                mockId,
                "Ana Pérez",
                "ana.perez@usantoto.edu.co",
                "password123",
                null));
        assertEquals("El rol del usuario no puede ser nulo", exception.getMessage());
    }
}
