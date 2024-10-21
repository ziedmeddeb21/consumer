package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MovieService {
    @Inject
    MovieRepository movieRepository;

    @Inject
    MappingRuleService mappingRuleService;

    public void createMovie(String movie) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode movieJson = objectMapper.readTree(movie);

        // Get all mapping rules
        List<Document> allMappingRules = mappingRuleService.getAllMappingRules();

        // Create a new JSON object to store the transformed movie
        ObjectNode transformedMovie = objectMapper.createObjectNode();

        // Iterate through mapping rules
        for (Document mappingRule : allMappingRules) {
            String sourceField = mappingRule.getString("source_field");
            String targetField = mappingRule.getString("target_field");

            // Check if the source field exists in the JSON object
            if (movieJson.has(sourceField)) {

                JsonNode value = movieJson.get(sourceField);
                transformedMovie.set(targetField, value);
                System.out.println("sourceField: " + sourceField + " targetField: " + targetField + " value: " + value);
            }
        }

        // Add the transformed movie to the repository
        movieRepository.addMovie(transformedMovie);
    }
}
