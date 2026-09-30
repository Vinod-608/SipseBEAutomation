package org.jarfinApiBackendAutomation.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties props = new Properties();

    static {
        try (InputStream in =
                ConfigLoader.class
                        .getClassLoader()
                        .getResourceAsStream("TestData/config.properties")) {
            if (in == null) {
                throw new RuntimeException(
                        "config.properties not found on classpath. Copy config.properties.example"
                                + " to config.properties and fill in values.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Missing required config key: " + key);
        }
        return value;
    }
}
