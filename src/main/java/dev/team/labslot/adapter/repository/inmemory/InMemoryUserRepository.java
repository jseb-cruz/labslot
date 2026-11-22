package dev.team.labslot.adapter.repository.inmemory;

import dev.team.labslot.core.dto.UserDTO;
import dev.team.labslot.core.entity.User;
import dev.team.labslot.core.port.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de usuarios en memoria. Guarda los usuarios en una lista interna.
 */
public class InMemoryUserRepository implements UserRepository {

    // Lista interna de almacenamiento
    private final ArrayList<User> users = new ArrayList<>();

    // Devuelve una copia inmutable para que nadie modifique la lista interna desde afuera
    @Override
    public List<User> findAll() {
        return List.copyOf(users);
    }

    // Busca un usuario por su id
    @Override
    public Optional<User> findById(UUID userId) {
        return users.stream()
                .filter(user -> user.userId().equals(userId))
                .findFirst();
    }

    // Crea un usuario con un id nuevo
    @Override
    public User create(UserDTO userRequest) {
        User user = new User(
                UUID.randomUUID(),
                userRequest.fullName(),
                userRequest.email(),
                userRequest.password(),
                userRequest.role()
        );
        users.add(user);
        return user;
    }

    // Actualiza un usuario existente conservando su id; vacío si no existe
    @Override
    public Optional<User> update(UUID userId, UserDTO userRequest) {
        for (User user : users) {
            if (user.userId().equals(userId)) {
                User updatedUser = new User(
                        user.userId(),
                        userRequest.fullName(),
                        userRequest.email(),
                        userRequest.password(),
                        userRequest.role()
                );
                users.set(users.indexOf(user), updatedUser);
                return Optional.of(updatedUser);
            }
        }
        return Optional.empty();
    }

    // Elimina un usuario por su id
    @Override
    public void delete(UUID userId) {
        users.removeIf(u -> u.userId().equals(userId));
    }
}
