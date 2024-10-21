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

        // Iterate through JSON attributes
        Iterator<Map.Entry<String, JsonNode>> fields = movieJson.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String sourceField = field.getKey();
            JsonNode value = field.getValue();
            System.out.println("sourceField: " + sourceField + " value: " + value);


//             Find the corresponding mapping rule
            for (Document mappingRule : allMappingRules) {
                if (mappingRule.getString("source_field").equals(sourceField)) {
                    String targetField = mappingRule.getString("target_field");
                    transformedMovie.set(targetField, value);
                    System.out.println("targetField: " + targetField + " value: " + value);
                    System.out.println("Transformed Movie: " + transformedMovie);
                    break;
                }
            }

        }
        movieRepository.addMovie(transformedMovie);



//
//        System.out.println("Transformed Movie: " + transformedMoive);
        // Add the transformed movie to the repository

    }
}
