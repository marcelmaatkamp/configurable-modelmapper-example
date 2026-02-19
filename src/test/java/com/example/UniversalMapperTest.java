package com.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UniversalMapperTest {

    private final String CONFIG_PATH = "target/test-classes/test-config.json";

    @BeforeEach
    void setUp() throws IOException {
        // Ensure the directory exists
        Files.createDirectories(Paths.get(CONFIG_PATH).getParent());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(CONFIG_PATH));
    }

    private void createConfigFile(String content) throws IOException {
        Files.write(Paths.get(CONFIG_PATH), content.getBytes());
    }

    @Test
    @DisplayName("Should map single object when config is valid")
    void shouldMapSingleObject_whenConfigIsValid() throws IOException, ConfigurationException, MappingException {
        String config = "{\n" +
                "  \"mappablePackages\": [\"com.example\"],\n" +
                "  \"mappings\": [\n" +
                "    {\n" +
                "      \"sourceClass\": \"com.example.Person\",\n" +
                "      \"targetClass\": \"com.example.Persoon\",\n" +
                "      \"fields\": {\n" +
                "        \"firstName\": \"voornaam\",\n" +
                "        \"lastName\": \"achternaam\",\n" +
                "        \"address\": \"adres\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"sourceClass\": \"com.example.Address\",\n" +
                "      \"targetClass\": \"com.example.Adres\",\n" +
                "      \"fields\": {\n" +
                "        \"street\": \"straat\",\n" +
                "        \"city\": \"stad\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        createConfigFile(config);

        UniversalMapper mapper = new UniversalMapper(CONFIG_PATH);
        Person source = new Person("John", "Doe", new Address("123 Main St", "Anytown"));

        Persoon target = mapper.map(source, Persoon.class);

        assertNotNull(target);
        assertEquals("John", target.getVoornaam());
        assertEquals("Doe", target.getAchternaam());
        assertNotNull(target.getAdres());
        assertEquals("123 Main St", target.getAdres().getStraat());
        assertEquals("Anytown", target.getAdres().getStad());
    }

    @Test
    @DisplayName("Should throw ConfigurationException when config file not found")
    void shouldThrowConfigurationException_whenConfigFileIsNotFound() {
        String nonExistentPath = "non/existent/path.json";
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            new UniversalMapper(nonExistentPath);
        });

        // Updated assertion to match the new, more descriptive error message
        assertTrue(exception.getMessage().contains("Resource not found on file system or classpath"));
    }

    @Test
    @DisplayName("Should throw FieldNotFoundException for invalid field in config")
    void shouldThrowFieldNotFoundException_forInvalidFieldInConfig() throws IOException {
        String config = "{\n" +
                "  \"mappablePackages\": [\"com.example\"],\n" +
                "  \"mappings\": [\n" +
                "    {\n" +
                "      \"sourceClass\": \"com.example.Person\",\n" +
                "      \"targetClass\": \"com.example.Persoon\",\n" +
                "      \"fields\": {\n" +
                "        \"invalidField\": \"voornaam\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        createConfigFile(config);

        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            new UniversalMapper(CONFIG_PATH);
        });

        assertTrue(exception.getCause() instanceof FieldNotFoundException);
        assertTrue(exception.getMessage().contains("Field 'invalidField' not found"));
    }
}
