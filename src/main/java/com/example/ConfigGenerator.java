package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigGenerator {

    // Regex corrected for Java 8 compatibility (double-escaped backslashes)
    private static final Pattern FIELD_PATTERN = Pattern.compile("(?:private|protected|public)\\s+(?:final\\s+|static\\s+)*([\\w<>.,?\\s\\[\\]]+)\\s+([a-zA-Z0-9_]+);");

    /**
     * Represents a field extracted from a Java source file.
     */
    private static class ClassField {
        final String type;
        final String name;

        ClassField(String type, String name) {
            this.type = type.trim();
            this.name = name.trim();
        }

        @Override
        public String toString() {
            return type + " " + name;
        }
    }

    /**
     * Extracts class and field information from a Java source file.
     */
    private static List<ClassField> extractFieldsFromFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<ClassField> fields = new ArrayList<>();
        String content = String.join("\n", lines);

        Matcher matcher = FIELD_PATTERN.matcher(content);
        while (matcher.find()) {
            fields.add(new ClassField(matcher.group(1), matcher.group(2)));
        }
        return fields;
    }

    private static String getClassNameFromFile(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        Matcher matcher = Pattern.compile("public\\s+class\\s+([\\w]+)").matcher(content);
        if (matcher.find()) {
            // This is a simplistic way to get the package and class name.
            // It assumes a standard package declaration.
             Matcher packageMatcher = Pattern.compile("package\\s+([\\w.]+);").matcher(content);
             if(packageMatcher.find()){
                 return packageMatcher.group(1) + "." + matcher.group(1);
             }
             // Fallback to just class name if no package is found
             return matcher.group(1);
        }
        throw new IOException("Could not determine class name from file: " + filePath);

    }


    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java com.example.ConfigGenerator <source-class-file-path> <target-class-file-path>");
            System.exit(1);
        }

        String sourceFilePath = args[0];
        String targetFilePath = args[1];

        List<ClassField> sourceFields = extractFieldsFromFile(sourceFilePath);
        List<ClassField> targetFields = extractFieldsFromFile(targetFilePath);
        String sourceClassName = getClassNameFromFile(sourceFilePath);
        String targetClassName = getClassNameFromFile(targetFilePath);

        System.out.println("Analyzing " + sourceFilePath + " and " + targetFilePath + "...\n");

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode mappingsNode = rootNode.putArray("mappings");
        ObjectNode mapping = mappingsNode.addObject();
        mapping.put("sourceClass", sourceClassName);
        mapping.put("targetClass", targetClassName);
        ObjectNode fieldsNode = mapping.putObject("fields");

        Scanner scanner = new Scanner(System.in);
        LevenshteinDistance levenshtein = new LevenshteinDistance();

        for (ClassField sourceField : sourceFields) {
            ClassField bestMatch = null;
            int bestScore = Integer.MAX_VALUE;

            for (ClassField targetField : targetFields) {
                 int distance = levenshtein.apply(sourceField.name, targetField.name);

                // Prioritize exact or case-insensitive matches
                if (sourceField.name.equalsIgnoreCase(targetField.name)) {
                    bestMatch = targetField;
                    break; // Perfect match found
                }
                // Consider fuzzy match if types are compatible
                // (simplistic check, can be improved)
                else if (distance < 3 && distance < bestScore) {
                    bestScore = distance;
                    bestMatch = targetField;
                }
            }

            if (bestMatch != null) {
                System.out.println("Found potential match:");
                System.out.println("  Source: " + sourceField.name + " (" + sourceField.type + ")");
                System.out.println("  Target: " + bestMatch.name + " (" + bestMatch.type + ")");
                System.out.print("Create this mapping? (y/n): ");
                String answer = scanner.nextLine();
                if (answer.equalsIgnoreCase("y")) {
                    fieldsNode.put(sourceField.name, bestMatch.name);
                    System.out.println("Mapping added.\n");
                }
            }
        }

        System.out.println("\n--- Generated JSON ---");
        System.out.println("Copy the following JSON into your 'mappings' array in config.json:\n");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapping));

        scanner.close();
    }
}
