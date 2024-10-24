package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;

import java.lang.reflect.Array;
import java.util.*;

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

//         Create a new JSON object to store the transformed movie
        ObjectNode transformedMovie = objectMapper.createObjectNode();

        ArrayNode arrayKeyVal = objectMapper.createArrayNode();
//        // Iterate through mapping rules
        for (Document mappingRule : allMappingRules) {
            String sourceField = mappingRule.getString("source_field");
            String targetField = mappingRule.getString("target_field");
            if (mappingRule.getBoolean("isKeyVal")) {
                //get the key and the value from the source
                String key = sourceField.substring(sourceField.lastIndexOf('/') + 1);
                JsonNode value = movieJson.at(sourceField);
                System.out.println("Key: " + key);
                System.out.println("Value: " + value);
                ObjectNode targetJson = objectMapper.createObjectNode();
                targetJson.put("key", key);
                targetJson.put("value", value);
                System.out.println("Target Json: " + targetJson);

                arrayKeyVal.add(targetJson);
                System.out.println("Target array: " + arrayKeyVal);


                // Split the target field path into components
                String[] targetPathComponents = targetField.split("/");
                // Navigate or create the necessary nodes in the transformedMovie
                //example /inf/metadata/title      Note: the one before the last element is the array name
                ObjectNode currentNode = transformedMovie;
                for (int i = 1; i < targetPathComponents.length - 1; i++) {
                    //if we reach the array name we stop the loop
                    if(i == targetPathComponents.length - 2){
                        break;
                    }
                    String pathComponent = targetPathComponents[i];
                    if (!currentNode.has(pathComponent)) {
                        currentNode.set(pathComponent, objectMapper.createObjectNode());
                    }
                    currentNode = (ObjectNode) currentNode.get(pathComponent);
                }
                //we create lastly the array and add the key value pair elements
                currentNode.set(targetPathComponents[targetPathComponents.length - 2], arrayKeyVal);



            }
            else{
            //if the source field is in an array example messages :[{"from":"me","to":"you"}]
            if (mappingRule.getBoolean("isArray")) {
                String fieldName = sourceField.split("/")[sourceField.split("/").length - 1];
                // get the target field name
                String targetFieldName = targetField.split("/")[targetField.split("/").length - 1];
                //get the exact path for the array in original json
                String arrayPath = sourceField.substring(0, sourceField.lastIndexOf('/'));

                //get the array of objects from the source exp : {"from":":me","to":"you"} {"from":":you","to":"me"}
                ArrayNode array = (ArrayNode) movieJson.at(arrayPath);
                //the new array where we will stock the transformed objects
                ArrayNode newArray = objectMapper.createArrayNode();
                //we loop through the array of objects using the current node to get the value of the object
                ObjectNode currentNode = objectMapper.createObjectNode();
                for (int i = 0; i < array.size(); i++) {
                    currentNode = (ObjectNode) array.get(i);
                    //get the value of the field name like the value of "from"
                    JsonNode value = currentNode.get(fieldName);
                    //we remove the field name from the object, and then we add the new field name with the same value
                    currentNode.remove(fieldName);
                    currentNode.set(targetFieldName, value);
                    //we add the new object to the new array
                    newArray.add(currentNode);


                }

                String[] targetPathComponents = targetField.substring(0, targetField.lastIndexOf('/')).split("/");
                //if the target field is at the root of the object
                if (targetPathComponents.length == 1) {
                    transformedMovie.set(targetPathComponents[0], newArray);
                }
                //else navigate to the target field and add the new array
                else {
                    ObjectNode navigatedNode = transformedMovie;
                    for (int i = 1; i < targetPathComponents.length - 1; i++) {
                        String pathComponent = targetPathComponents[i];
                        navigatedNode = (ObjectNode) navigatedNode.get(pathComponent);
                    }
                    String destinationArrayName = targetField.split("/")[targetField.split("/").length - 2];
                    navigatedNode.set(destinationArrayName, newArray);
                }


            } else {
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

                //if it's an array then initizalize it as an empty array
                if (mappingRule.getString("field_type").equals("Array")) {
                    String finalPathComponent = targetPathComponents[targetPathComponents.length - 1];
                    ArrayNode emptyArray = objectMapper.createArrayNode();
                    currentNode.set(finalPathComponent, emptyArray);

                }
                //if it's a normal node then set the value
                else {
                    String finalPathComponent = targetPathComponents[targetPathComponents.length - 1];
                    currentNode.set(finalPathComponent, movieJson.at(sourceField));
                }

            }
        }
        }


        System.out.println("transformedMovie: " + transformedMovie);

//         Add the transformed movie to the repository
         movieRepository.addMovie(transformedMovie);
    }


}
