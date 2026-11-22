package dev.team.labslot.core.port;

/**
 * Puerto para cifrar contraseñas. El core no sabe con qué algoritmo (el adaptador usa BCrypt).
 */
public interface PasswordHasher {

    // Devuelve el hash de la contraseña; es lo que se guarda, nunca el texto original
    String hash(String rawPassword);
}
