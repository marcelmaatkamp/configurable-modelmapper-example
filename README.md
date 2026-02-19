# Configurable Model Mapper

This project provides a dynamic, configuration-driven object mapping utility for Java. It allows you to map objects of different classes based on an external JSON configuration file, avoiding hardcoded mapping logic and increasing flexibility.

The core of the project is the `ConfigurableModelMapper` class, which uses reflection and a JSON configuration to map fields between source and destination objects, including nested objects.

## Features

- **Dynamic Mapping:** Define mappings in a JSON file without changing the source code.
- **Nested Object Mapping:** Automatically handles mapping of nested custom objects.
- **Type Conversion:** Basic support for type conversion (e.g., `String` to `enum`).
- **Configuration Generator:** A utility class, `ConfigGenerator`, is provided to inspect two classes and interactively generate a JSON mapping configuration.

## Project Structure

The project is organized into the following main packages:

- `com.example`: Contains the core application logic, including the mapper (`ConfigurableModelMapper`), the main application entry point (`Application`), and the configuration generator (`ConfigGenerator`).
- `com.example.models`: Contains all the Plain Old Java Objects (POJOs) or data model classes.
- `src/main/resources`: Contains the `config.json` file used to configure the mappings.

## How to Use

### Prerequisites

- Java 8 or higher
- Apache Maven

### 1. (Optional) Generate a Mapping Configuration

If you need to create a new mapping configuration between two classes, you can use the `ConfigGenerator` utility. It will interactively ask you to confirm mappings between fields based on name similarity.

1.  **Run the generator from the command line**, providing the full paths to the source and target Java files:

    ```sh
    mvn compile exec:java -Dexec.mainClass="com.example.ConfigGenerator" -Dexec.args="src/main/java/com/example/models/Person.java src/main/java/com/example/models/Persoon.java"
    ```

2.  **Follow the prompts** in your terminal to confirm the field mappings.

3.  **Copy the generated JSON** output from the terminal into your `src/main/resources/config.json` file. The output will look something like this:

    ```json
    {
      "sourceClass": "com.example.models.Person",
      "targetClass": "com.example.models.Persoon",
      "fields": {
        "firstName": "voornaam",
        "lastName": "achternaam",
        "address": "adres"
      }
    }
    ```

    *Make sure to embed this object inside the `mappings` array in `config.json`.*

### 2. Run the Main Application

The `Application.java` class contains a `main` method that demonstrates a single object mapping and a collection mapping using the configuration from `config.json`.

To run the application, execute the following Maven command:

```sh
mvn compile exec:java -Dexec.mainClass="com.example.Application"
```

This will execute the main method in `com.example.Application` and you will see output in your console demonstrating the successful mapping of a `Person` object to a `Persoon` object, as well as a list of `Person` objects to a list of `Persoon` objects.

### 3. Run Tests

Unit tests for the `ConfigurableModelMapper` are located in `src/test/java/com/example/ConfigurableModelMapperTest.java`.

To run the tests, use the following Maven command:

```sh
mvn test
```

This will compile and run the tests, verifying that the mapping logic works as expected.