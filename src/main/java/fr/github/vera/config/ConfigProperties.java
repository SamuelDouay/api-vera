package fr.github.vera.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class ConfigProperties {
    private static final Logger logger = LogManager.getLogger(ConfigProperties.class);
    private static ConfigProperties instance;
    private final Properties properties;

    ConfigProperties() {
        this.properties = new Properties();
        loadFile();
    }

    public static ConfigProperties getInstance() {
        if (instance == null) {
            instance = new ConfigProperties();
        }
        return instance;
    }

    private static String resolvePlaceholders(String value) {
        if (value.startsWith("${") && value.endsWith("}")) {
            String envVar = value.substring(2, value.length() - 1);
            String[] parts = envVar.split(";");

            String varName = parts[0];
            String defaultValue = parts.length > 1 ? parts[1] : "";

            // Priorité : variable d'environnement > valeur par défaut
            String envValue = System.getenv(varName);
            return envValue != null ? envValue : defaultValue;
        }
        return value;
    }

    private void loadFile() {
        try {
            this.properties.load(getClass().getResourceAsStream("/setting/database.properties"));
            logger.debug("Load database properties file");

            for (String key : this.properties.stringPropertyNames()) {
                String value = this.properties.getProperty(key);
                this.properties.setProperty(key, resolvePlaceholders(value));
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }
}