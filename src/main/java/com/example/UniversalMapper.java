package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UniversalMapper {

    private static final Logger logger = LoggerFactory.getLogger(UniversalMapper.class);
    private final ModelMapper modelMapper;
    private final Map<Class<?>, Map<String, Field>> fieldCache = new ConcurrentHashMap<>();
    private List<String> mappablePackages = new ArrayList<>();

    public UniversalMapper(String configPath) throws ConfigurationException {
        this.modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);

        configureFromJSON(configPath);
    }

    @SuppressWarnings("unchecked")
    private void configureFromJSON(String configPath) throws ConfigurationException {
        ObjectMapper mapper = new ObjectMapper();

        InputStream inputStream = null;
        try {
            File configFile = new File(configPath);
            if (configFile.exists() && !configFile.isDirectory()) {
                logger.debug("Loading configuration from file system: {}", configPath);
                inputStream = new FileInputStream(configFile);
            } else {
                logger.debug("File not found on file system, trying classpath: {}", configPath);
                String correctedPath = configPath.startsWith("/") ? configPath.substring(1) : configPath;
                inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(correctedPath);
                if (inputStream == null) {
                    throw new ConfigurationException("Resource not found on file system or classpath: " + configPath);
                }
            }

            JsonNode rootNode = mapper.readTree(inputStream);

            JsonNode mappablePackagesNode = rootNode.get("mappablePackages");
            if (mappablePackagesNode != null && mappablePackagesNode.isArray()) {
                for (JsonNode pkgNode : mappablePackagesNode) {
                    mappablePackages.add(pkgNode.asText());
                }
            }

            JsonNode mappingsNode = rootNode.get("mappings");

            if (mappingsNode != null && mappingsNode.isArray()) {
                for (JsonNode node : mappingsNode) {
                    String sourceClassName = node.get("sourceClass").asText();
                    String targetClassName = node.get("targetClass").asText();

                    Class<?> sourceClass = Class.forName(sourceClassName);
                    Class<?> targetClass = Class.forName(targetClassName);

                    TypeMap<Object, Object> typeMap = (TypeMap<Object, Object>) modelMapper.createTypeMap(sourceClass, targetClass);

                    Map<String, String> fieldMappings = new HashMap<>();
                    JsonNode fields = node.get("fields");
                    Iterator<String> fieldNames = fields.fieldNames();

                    while (fieldNames.hasNext()) {
                        String sourceFieldName = fieldNames.next();
                        String targetFieldName = fields.get(sourceFieldName).asText();
                        validateFieldExists(sourceClass, sourceFieldName);
                        validateFieldExists(targetClass, targetFieldName);
                        fieldMappings.put(sourceFieldName, targetFieldName);
                    }

                    Converter<Object, Object> converter = new AbstractConverter<Object, Object>() {
                        @Override
                        protected Object convert(Object source) {
                            try {
                                Object destination = targetClass.getDeclaredConstructor().newInstance();
                                for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
                                    String sourceField = entry.getKey();
                                    String targetField = entry.getValue();

                                    Object value = getReflectionValue(source, sourceField);

                                    if (isCustomObject(value)) {
                                        Object nestedTarget = map(value, findField(destination.getClass(), targetField).getType());
                                        setReflectionValue(destination, targetField, nestedTarget);
                                    } else {
                                        setReflectionValue(destination, targetField, value);
                                    }
                                }
                                return destination;
                            } catch (Exception e) {
                                throw new RuntimeException(new MappingException("Failed to convert object from " + source.getClass().getName() + " to " + targetClass.getName(), e));
                            }
                        }
                    };

                    typeMap.setConverter(converter);
                }
            }
        } catch (Exception e) {
            throw new ConfigurationException("Failed to configure mapper from JSON: " + e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.warn("Failed to close configuration input stream", e);
                }
            }
        }
    }

    private boolean isCustomObject(Object obj) {
        if (obj == null) {
            return false;
        }
        String packageName = obj.getClass().getPackage().getName();
        return mappablePackages.stream().anyMatch(packageName::startsWith);
    }

    private Object getReflectionValue(Object source, String fieldName) throws FieldNotFoundException, IllegalAccessException {
        Field field = findField(source.getClass(), fieldName);
        field.setAccessible(true);
        return field.get(source);
    }

    private void setReflectionValue(Object dest, String fieldName, Object value) throws FieldNotFoundException, IllegalAccessException {
        Field field = findField(dest.getClass(), fieldName);
        field.setAccessible(true);
        field.set(dest, value);
    }

    private Field findField(Class<?> clazz, String fieldName) throws FieldNotFoundException {
        fieldCache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());
        Map<String, Field> classCache = fieldCache.get(clazz);

        if (classCache.containsKey(fieldName)) {
            return classCache.get(fieldName);
        }

        Class<?> current = clazz;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                classCache.put(fieldName, field);
                return field;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new FieldNotFoundException("Field '" + fieldName + "' not found in class hierarchy for " + clazz.getName());
    }

    private void validateFieldExists(Class<?> clazz, String fieldName) throws FieldNotFoundException {
        findField(clazz, fieldName);
    }

    public <S, T> T map(S source, Class<T> targetClass) throws MappingException {
        try {
            return modelMapper.map(source, targetClass);
        } catch (Exception e) {
            throw new MappingException("Failed to map object from " + source.getClass().getName() + " to " + targetClass.getName(), e);
        }
    }

    public <S, T> List<T> map(Collection<S> source, Class<T> targetClass) throws MappingException {
        if (source == null) {
            return Collections.emptyList();
        }
        List<T> results = new ArrayList<>();
        for (S element : source) {
            results.add(map(element, targetClass));
        }
        return results;
    }

    public static void main(String[] args) {
        try {
            // Now uses the classpath resource, as before
            UniversalMapper myMapper = new UniversalMapper("config.json");
            logger.info("Configuration loaded successfully!");

            logger.info("--- Single Object Mapping Test ---");
            Address address = new Address("Main St", "Anytown");
            Person p = new Person("Jan", "Jansen", address);
            logger.info("Source: {}", p);

            Persoon result = myMapper.map(p, Persoon.class);

            logger.info("Target: {}", result);
            logger.info("Single object mapping successful!");

            logger.info("\n--- Collection Mapping Test ---");
            List<Person> personList = Arrays.asList(
                    new Person("Piet", "Pietersen", new Address("Second St", "Othertown")),
                    new Person("Klaas", "Klaassen", new Address("Third Ave", "Anotherville"))
            );

            logger.info("Source List:");
            personList.forEach(person -> logger.info("  {}", person));

            List<Persoon> persoonListResult = myMapper.map(personList, Persoon.class);

            logger.info("Target List:");
            persoonListResult.forEach(persoon -> logger.info("  {}", persoon));
            logger.info("Collection mapping successful!");

        } catch (ConfigurationException e) {
            logger.error("Configuration Error: {}", e.getMessage(), e);
        } catch (MappingException e) {
            logger.error("Mapping Error: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred in the UniversalMapper main method", e);
        }
    }
}
