package dev.team.labslot.adapter.security;

import dev.team.labslot.core.port.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Cifra las contraseñas con BCrypt. El login (SecurityConfig) las compara también con BCrypt.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}
