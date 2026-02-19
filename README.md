# Configurable Model Mapper

This project provides a dynamic, configuration-driven object mapping utility for Java. It allows you to map objects of different classes based on an external JSON configuration file, avoiding hardcoded mapping logic and increasing flexibility.

## Key Features

- **Configuration-Driven:** Define all your mappings in a single JSON file. No need to recompile to change mapping logic.
- **Deep Object Mapping:** This is a core feature. The mapper can recursively map nested objects. For example, if a `Person` object contains an `Address` object, it will use a separate mapping definition to convert the `Address` to an `Adres` object, creating a complete, deep copy.
- **Type Conversion:** Basic support for type conversion (e.g., `String` to `enum`).
- **Configuration Generator:** A utility class, `ConfigGenerator`, helps bootstrap your configuration by interactively generating a JSON template.

## How it Works: Deep Mapping Example

The power of this solution lies in its ability to handle nested objects. This is achieved through the `mappablePackages` property and by defining mappings for each level of the object graph in `config.json`.

Consider this `config.json`:
```json
{
  "mappablePackages": ["com.example.models"],
  "mappings": [
    {
      "sourceClass": "com.example.models.Person",
      "targetClass": "com.example.models.Persoon",
      "fields": {
        "firstName": "voornaam",
        "lastName": "achternaam",
        "address": "adres"
      }
    },
    {
      "sourceClass": "com.example.models.Address",
      "targetClass": "com.example.models.Adres",
      "fields": {
        "street": "straat",
        "city": "stad"
      }
    }
  ]
}
```

1. When mapping `Person` to `Persoon`, the mapper processes the `address` field.
2. It recognizes that the `Address` object's package (`com.example.models`) is listed in `mappablePackages`.
3. Instead of a simple assignment, it searches the `mappings` array for an entry where `sourceClass` is `com.example.models.Address`.
4. It finds the `Address` -> `Adres` mapping and **recursively calls itself** to map the nested object.
5. The resulting `Adres` object is then assigned to the `adres` field of the parent `Persoon` object.

This mechanism allows for complex, multi-level object transformations defined entirely in the configuration.

## How to Use

### Prerequisites

- Java 8 or higher
- Apache Maven

### 1. (Optional) Generate a Mapping Configuration

If you need to create a new mapping configuration, use the `ConfigGenerator` utility. Provide the full paths to the source and target Java files.

```sh
mvn compile exec:java -Dexec.mainClass="com.example.ConfigGenerator" -Dexec.args="src/main/java/com/example/models/Person.java src/main/java/com/example/models/Persoon.java"
```

Follow the prompts and copy the generated JSON for each class into the `mappings` array in `src/main/resources/config.json`.

### 2. Run the Main Application

The `Application.java` class demonstrates the mapping, including the deep mapping of the address object.

To run the application, execute:

```sh
mvn compile exec:java -Dexec.mainClass="com.example.Application"
```

You will see output demonstrating the successful mapping of a `Person` object (with a nested `Address`) to a `Persoon` object (with a nested `Adres`).

### 3. Run Tests

To verify the mapping logic with unit tests, run:

```sh
mvn test
```
