# Configurable ModelMapper Example

This project demonstrates a dynamic, configuration-driven object mapper for Java. It allows you to map objects of different classes based on a JSON configuration file, providing flexibility and avoiding hardcoded mapping logic.

The core of the project is the `UniversalMapper` class, which takes a source object, a destination class, and a configuration file path to perform the mapping. The configuration file specifies which fields in the source object map to which fields in the destination object.

## Getting Started

A `ConfigGenerator` class is also included to automatically generate a basic configuration file by inspecting the fields of the source and destination classes.

## How to Get Started

1. **Prerequisites:**
   - Java 8 or higher
   - Maven

2. **Run the Config Generator:**
   To generate the mapping configuration, you can run the `ConfigGenerator` using Maven:
   ```sh
   mvn exec:java
   ```
   This will execute the `main` method in `com.example.ConfigGenerator` and create a `config.json` file in `src/main/resources`. The default source and destination classes are `com.example.Person` and `com.example.Persoon` respectively. You can modify the arguments in `pom.xml` to use different classes.

3. **Run the Tests:**
   The `UniversalMapperTest` class contains a test case that demonstrates the usage of the `UniversalMapper`. To run the tests, use the following command:
   ```sh
   mvn test
   ```

## What to Expect

- After running the `ConfigGenerator`, you will find a `config.json` in the `src/main/resources` directory. This file will look something like this:
  ```json
  {
    "fieldMappings": {
      "name": "naam",
      "age": "leeftijd",
      "address": "adres"
    }
  }
  ```
- The `UniversalMapperTest` will pass, demonstrating that an instance of `Person` can be successfully mapped to an instance of `Persoon` using the generated `config.json`.
- You can extend this project by:
  - Adding more complex mapping scenarios.
  - Enhancing the `ConfigGenerator` to handle different data types and nested objects more intelligently.
  - Integrating this mapping solution into a larger application where dynamic object mapping is required.

This project serves as a practical example of how to build a flexible and configurable object mapping system in Java.