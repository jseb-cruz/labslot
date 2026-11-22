package dev.team.labslot.adapter.repository.mariadb.entity;

import dev.team.labslot.core.constant.UserRole;
import dev.team.labslot.core.entity.User;

import java.util.UUID;

/**
 * Fila de la tabla users. El id va como String porque la columna es VARCHAR(36).
 */
public record UserJdbi(
        String userId,
        String fullName,
        String email,
        String password,
        UserRole role
) {
    public static UserJdbi fromDomain(User user) {
        return new UserJdbi(
                user.userId().toString(),
                user.fullName(),
                user.email(),
                user.password(),
                user.role()
        );
    }

    public User toDomain() {
        return new User(UUID.fromString(userId), fullName, email, password, role);
    }
}
