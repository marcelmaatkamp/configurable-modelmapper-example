package com.example;

import com.example.models.Address;
import com.example.models.Person;
import com.example.models.Persoon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        try {
            // Now uses the classpath resource, as before
            ConfigurableModelMapper myMapper = new ConfigurableModelMapper("config.json");
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
            logger.error("An unexpected error occurred in the Application main method", e);
        }
    }
}
