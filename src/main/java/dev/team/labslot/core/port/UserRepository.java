package dev.team.labslot.core.port;

import dev.team.labslot.core.dto.UserDTO;
import dev.team.labslot.core.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto con las operaciones de persistencia de usuarios.
 */
public interface UserRepository {

    // Devuelve todos los usuarios
    List<User> findAll();

    // Busca un usuario por su id
    Optional<User> findById(UUID userId);

    // Crea un usuario nuevo a partir del DTO
    User create(UserDTO userRequest);

    // Actualiza un usuario existente; vacío si no existe
    Optional<User> update(UUID userId, UserDTO userRequest);

    // Elimina un usuario por su id
    void delete(UUID userId);
}
