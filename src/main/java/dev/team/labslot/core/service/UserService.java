package dev.team.labslot.core.service;

import dev.team.labslot.core.constant.Constants;
import dev.team.labslot.core.dto.UserDTO;
import dev.team.labslot.core.entity.User;
import dev.team.labslot.core.port.PasswordHasher;
import dev.team.labslot.core.port.ReservationRepository;
import dev.team.labslot.core.port.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserService {
    private final UserRepository userRepository;
    // Se usa para no eliminar usuarios que tengan reservas
    private final ReservationRepository reservationRepository;
    // Cifra las contraseñas (el core no sabe que se usa BCrypt)
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository userRepository, ReservationRepository reservationRepository,
                       PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.passwordHasher = passwordHasher;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("El id del usuario no puede ser nulo");
        }
        return userRepository.findById(userId);
    }

    // Busca un usuario por email sin distinguir mayúsculas (lo usan el login y las vistas)
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email del usuario no puede ser nulo o vacío");
        }
        return userRepository.findAll().stream()
                .filter(user -> user.email().equalsIgnoreCase(email))
                .findFirst();
    }

    // La contraseña llega en texto y se guarda cifrada
    public User create(UserDTO userRequest) {
        if (userRequest == null) {
            throw new IllegalArgumentException("La solicitud de usuario no puede ser nula");
        }
        // El email es único sin distinguir mayúsculas
        boolean emailAlreadyExists = userRepository.findAll().stream()
                .anyMatch(user -> user.email().equalsIgnoreCase(userRequest.email()));
        if (emailAlreadyExists) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }
        return userRepository.create(withHashedPassword(userRequest));
    }

    // Igual que al crear: la contraseña llega en texto y se guarda cifrada
    public Optional<User> update(UUID userId, UserDTO userRequest) {
        if (userId == null) {
            throw new IllegalArgumentException("El id del usuario no puede ser nulo");
        }
        if (userRequest == null) {
            throw new IllegalArgumentException("La solicitud de usuario no puede ser nula");
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("No existe el usuario a actualizar");
        }
        // Se excluye el propio usuario para que pueda conservar su email
        boolean emailAlreadyExists = userRepository.findAll().stream()
                .anyMatch(user -> !user.userId().equals(userId)
                        && user.email().equalsIgnoreCase(userRequest.email()));
        if (emailAlreadyExists) {
            throw new IllegalArgumentException("Ya existe otro usuario con ese email");
        }
        return userRepository.update(userId, withHashedPassword(userRequest));
    }

    // requestedByUserId es el usuario que pide eliminar: nadie puede eliminarse a sí mismo.
    // Como solo elimina un administrador, así siempre queda al menos uno
    public void delete(UUID userId, UUID requestedByUserId) {
        if (userId == null) {
            throw new IllegalArgumentException("El id del usuario no puede ser nulo");
        }
        if (requestedByUserId == null) {
            throw new IllegalArgumentException("El id del usuario que solicita la eliminación no puede ser nulo");
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("No existe el usuario a eliminar");
        }
        if (userId.equals(requestedByUserId)) {
            throw new IllegalArgumentException("No puedes eliminar tu propio usuario");
        }
        // Cuenta cualquier reserva, sin importar su estado, para no dejar reservas huérfanas
        boolean hasReservations = reservationRepository.findAll().stream()
                .anyMatch(reservation -> reservation.userId().equals(userId));
        if (hasReservations) {
            throw new IllegalArgumentException("No se puede eliminar un usuario con reservas");
        }
        userRepository.delete(userId);
    }

    // Valida la contraseña en texto y devuelve la misma solicitud con la contraseña cifrada.
    // Se valida antes de cifrar porque el hash siempre tiene la misma longitud y nunca está vacío
    private UserDTO withHashedPassword(UserDTO userRequest) {
        String password = userRequest.password();
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña del usuario no puede ser nula o vacía");
        }
        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("La contraseña del usuario debe tener al menos "
                    + Constants.MIN_PASSWORD_LENGTH + " caracteres");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > Constants.MAX_PASSWORD_BYTES) {
            throw new IllegalArgumentException("La contraseña del usuario es demasiado larga (máximo "
                    + Constants.MAX_PASSWORD_BYTES + " caracteres; las tildes y la ñ cuentan doble)");
        }
        return new UserDTO(
                userRequest.fullName(),
                userRequest.email(),
                passwordHasher.hash(password),
                userRequest.role()
        );
    }
}
