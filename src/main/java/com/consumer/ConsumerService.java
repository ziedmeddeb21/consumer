package com.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapping_rules.MappingRuleService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class ConsumerService {
    @Inject
    ConsumerRepository consumerRepository;

    @Inject
    MappingRuleService mappingRuleService;

    private final Logger logger = Logger.getLogger(ConsumerService.class);

    @Incoming("json-in")
    public void receive(String payload) throws JsonProcessingException {
        mapPayload(payload, getAllMappingRules());

    }

    public ObjectNode mapPayload(String payload ,List<Document> allRules) throws JsonProcessingException {
        System.out.println("Payload: " + payload);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadJson = objectMapper.readTree(payload);
        ObjectNode transformedPayload = objectMapper.createObjectNode();
        ArrayNode arrayKeyVal = objectMapper.createArrayNode();

        for (Document mappingRule : allRules) {
            String sourceField = mappingRule.getString("source_field");
            String targetField = mappingRule.getString("target_field");
            boolean isKeyVal = mappingRule.getBoolean("isKeyVal");
            boolean belongsToArray = mappingRule.getBoolean("belongsToArray");
            boolean isArray = mappingRule.getBoolean("isArray");

            if (isKeyVal) {
                handleKeyValMapping(objectMapper, payloadJson, transformedPayload, arrayKeyVal, sourceField, targetField);
            } else if (belongsToArray) {
                handleArrayMapping(objectMapper, payloadJson, transformedPayload, sourceField, targetField);
            } else {
                handleStandardMapping(objectMapper, payloadJson, transformedPayload, sourceField, targetField, isArray);
            }
        }

        System.out.println("transformedPayload: " + transformedPayload);
        logger.infof("Transformed payload: %s", transformedPayload);
        consumerRepository.addpayload(transformedPayload);
        return transformedPayload;

    }

    private void handleKeyValMapping(ObjectMapper objectMapper, JsonNode payloadJson, ObjectNode transformedPayload, ArrayNode arrayKeyVal, String sourceField, String targetField) {
        String[] targetPathComponents = targetField.split("/");
        // key is the default name for the key field
        // keyName is the custom name of the key field
        // valName is the custom name of the value field
        // example /inf/metadata/title/newKeyName/newKeyVal     Note: the one before the last element is the array name
        String key = targetPathComponents[targetPathComponents.length - 3];
        String keyName = targetPathComponents[targetPathComponents.length - 2];
        String valName = targetPathComponents[targetPathComponents.length - 1];
        JsonNode value = payloadJson.at(sourceField);
        ObjectNode targetJson = objectMapper.createObjectNode();
        targetJson.put(keyName, key);
        targetJson.set(valName, value);


        ObjectNode currentNode = navigateToNode(objectMapper, transformedPayload, targetPathComponents, targetPathComponents.length - 4);

        if (currentNode.has(targetPathComponents[targetPathComponents.length - 3])) {
            arrayKeyVal.add(targetJson);
        } else {
            arrayKeyVal = objectMapper.createArrayNode();
            arrayKeyVal.add(targetJson);
        }

        currentNode.set(targetPathComponents[targetPathComponents.length - 4], arrayKeyVal);
    }

    private void handleArrayMapping(ObjectMapper objectMapper, JsonNode payloadJson, ObjectNode transformedPayload, String sourceField, String targetField) {
        String fieldName = sourceField.split("/")[sourceField.split("/").length - 1];
        String targetFieldName = targetField.split("/")[targetField.split("/").length - 1];
        String arrayPath = sourceField.substring(0, sourceField.lastIndexOf('/'));
        ArrayNode array = (ArrayNode) payloadJson.at(arrayPath);
        ArrayNode newArray = objectMapper.createArrayNode();

        for (JsonNode node : array) {
            ObjectNode currentNode = (ObjectNode) node;
            JsonNode value = currentNode.get(fieldName);
            currentNode.remove(fieldName);
            currentNode.set(targetFieldName, value);
            newArray.add(currentNode);
        }

        String[] targetPathComponents = targetField.substring(0, targetField.lastIndexOf('/')).split("/");
        ObjectNode navigatedNode = navigateToNode(objectMapper, transformedPayload, targetPathComponents, targetPathComponents.length - 1);
        navigatedNode.set(targetPathComponents[targetPathComponents.length - 1], newArray);
    }

    private void handleStandardMapping(ObjectMapper objectMapper, JsonNode payloadJson, ObjectNode transformedPayload, String sourceField, String targetField, boolean isArray) {
        String[] targetPathComponents = targetField.split("/");
        ObjectNode currentNode = navigateToNode(objectMapper, transformedPayload, targetPathComponents, targetPathComponents.length - 1);

        if (isArray) {
            currentNode.set(targetPathComponents[targetPathComponents.length - 1], objectMapper.createArrayNode());
        } else {
            currentNode.set(targetPathComponents[targetPathComponents.length - 1], payloadJson.at(sourceField));
        }
    }

    private ObjectNode navigateToNode(ObjectMapper objectMapper, ObjectNode rootNode, String[] pathComponents, int endIndex) {
        ObjectNode currentNode = rootNode;
        for (int i = 1; i < endIndex; i++) {
            String pathComponent = pathComponents[i];
            if (!currentNode.has(pathComponent)) {
                currentNode.set(pathComponent, objectMapper.createObjectNode());
            }
            currentNode = (ObjectNode) currentNode.get(pathComponent);
        }
        return currentNode;
    }

    private List<Document> getAllMappingRules() {
        return mappingRuleService.getAllMappingRules();
    }





}