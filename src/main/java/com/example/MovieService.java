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
import java.util.SortedSet;

@ApplicationScoped
public class MovieService {
    @Inject
    MovieRepository movieRepository;

    @Inject
    MappingRuleService mappingRuleService;

    public void createMovie(String movie) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode movieJson = objectMapper.readTree(movie);

        System.out.println("\nmovieJson: " + movieJson);

        // Get all mapping rules
        List<Document> allMappingRules = mappingRuleService.getAllMappingRules();

//         Create a new JSON object to store the transformed movie
        ObjectNode transformedMovie = objectMapper.createObjectNode();
//        // Iterate through mapping rules
        for (Document mappingRule : allMappingRules) {
            String sourceField = mappingRule.getString("source_field");
            String targetField = mappingRule.getString("target_field");
            // Split the target field path into components
            String[] targetPathComponents = targetField.split("/");
            // Navigate or create the necessary nodes in the transformedMovie
            ObjectNode currentNode = transformedMovie;
            for (int i = 1; i < targetPathComponents.length - 1; i++) {
                String pathComponent = targetPathComponents[i];
                if (!currentNode.has(pathComponent)) {
                    currentNode.set(pathComponent, objectMapper.createObjectNode());
                }
                currentNode = (ObjectNode) currentNode.get(pathComponent);
            }

            // Set the value at the correct location
            String finalPathComponent = targetPathComponents[targetPathComponents.length - 1];
            currentNode.set(finalPathComponent, movieJson.at(sourceField));
                System.out.println("sourceField: " + sourceField + " targetField: " + targetField + " value: " +  movieJson.at(sourceField).asText());


        }
        System.out.println("transformedMovie: " + transformedMovie);

        // Add the transformed movie to the repository
//        movieRepository.addMovie(transformedMovie);
    }


}
