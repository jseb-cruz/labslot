package dev.team.labslot.adapter.repository.mariadb;

import dev.team.labslot.adapter.repository.mariadb.entity.UserJdbi;
import dev.team.labslot.adapter.repository.mariadb.repositories.UserDao;
import dev.team.labslot.core.dto.UserDTO;
import dev.team.labslot.core.entity.User;
import dev.team.labslot.core.port.UserRepository;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de usuarios con JDBI. Se crea con @Bean en MariaDbConfig.
 */
public class MariaDbUserRepository implements UserRepository {

    private final UserDao userDao;

    public MariaDbUserRepository(Jdbi jdbi) {
        // onDemand abre y cierra una conexión en cada llamada
        this.userDao = jdbi.onDemand(UserDao.class);
    }

    // Devuelve todos los usuarios
    @Override
    public List<User> findAll() {
        return userDao.findAll()
                .stream()
                .map(UserJdbi::toDomain)
                .toList();
    }

    // Busca un usuario por su id
    @Override
    public Optional<User> findById(UUID userId) {
        return userDao.findById(userId.toString())
                .map(UserJdbi::toDomain);
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
        userDao.insert(UserJdbi.fromDomain(user));
        return user;
    }

    // Actualiza un usuario existente conservando su id; vacío si no existe
    @Override
    public Optional<User> update(UUID userId, UserDTO userRequest) {
        return findById(userId)
                .map(existing -> {
                    User updatedUser = new User(
                            existing.userId(),
                            userRequest.fullName(),
                            userRequest.email(),
                            userRequest.password(),
                            userRequest.role()
                    );
                    userDao.update(UserJdbi.fromDomain(updatedUser));
                    return updatedUser;
                });
    }

    // Elimina un usuario por su id
    @Override
    public void delete(UUID userId) {
        userDao.delete(userId.toString());
    }
}
