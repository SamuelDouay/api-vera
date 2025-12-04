package fr.github.vera.security;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PasswordEncoder {
    private static final Logger LOGGER = LogManager.getLogger(PasswordEncoder.class);
    private static final int ITERATIONS = 10;     // Nombre d'itérations
    private static final int MEMORY = 65536;      // 64MB de mémoire
    private static final int PARALLELISM = 1;     // Threads parallèles
    private final Argon2 argon2;

    public PasswordEncoder() {
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    }

    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, plainPassword.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du hashage du password", e);
        } finally {
            argon2.wipeArray(plainPassword.toCharArray()); // Nettoyer la mémoire
        }
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        try {
            return argon2.verify(hashedPassword, plainPassword.toCharArray());
        } catch (Exception e) {
            LOGGER.error("Erreur vérification password: {}", e.getMessage());
            return false;
        }
    }
}