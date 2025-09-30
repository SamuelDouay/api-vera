package fr.github.vera.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class ConfigProperties {
    private static final Logger logger = LogManager.getLogger();
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

    private void loadFile() {
        try {
            this.properties.load(getClass().getResourceAsStream("/setting/database.properties"));
            logger.debug("Load database properties file");
        } catch (IOException e) {
           throw new RuntimeException(e.getMessage());
        }
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }
}