package dev.team.labslot.core.service;

import dev.team.labslot.core.dto.UserDTO;
import dev.team.labslot.core.entity.User;
import dev.team.labslot.core.port.ReservationRepository;
import dev.team.labslot.core.port.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserService {
    private final UserRepository userRepository;
    // Se usa para no eliminar usuarios que tengan reservas
    private final ReservationRepository reservationRepository;

    public UserService(UserRepository userRepository, ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
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
        return userRepository.create(userRequest);
    }

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
        return userRepository.update(userId, userRequest);
    }

    public void delete(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("El id del usuario no puede ser nulo");
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("No existe el usuario a eliminar");
        }
        // Cuenta cualquier reserva, sin importar su estado, para no dejar reservas huérfanas
        boolean hasReservations = reservationRepository.findAll().stream()
                .anyMatch(reservation -> reservation.userId().equals(userId));
        if (hasReservations) {
            throw new IllegalArgumentException("No se puede eliminar un usuario con reservas");
        }
        userRepository.delete(userId);
    }
}
