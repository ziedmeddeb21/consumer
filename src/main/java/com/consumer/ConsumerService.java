package com.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mappingRules.MappingRuleService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@ApplicationScoped
public class ConsumerService {
    @Inject
    ConsumerRepository consumerRepository;

    @Inject
    MappingRuleService mappingRuleService;

    private final Logger logger = Logger.getLogger(ConsumerService.class);
    ObjectMapper objectMapper = new ObjectMapper();
    ArrayNode arrayKeyVal;
    @Inject
    @Channel("output")
    Emitter<String> emitter;
    @Incoming("json-in")


    public void receive(String payload) throws JsonProcessingException {
        JsonNode initialPayload = objectMapper.readTree(payload);
        JsonNode payloadJson = initialPayload.get("content");

        String collectionName = initialPayload.get("collectionName").asText();
        System.out.println("collection name: "+collectionName);
        mapPayload(payloadJson, getAllMappingRules(collectionName));

    }

    public ObjectNode mapPayload(JsonNode payloadJson ,List<Document> allRules) throws JsonProcessingException {
//        System.out.println("Payload: " + payload);
        try{
        // I used this because the original payload can be modified
        JsonNode payloadJsonCopy = payloadJson.deepCopy();
        AtomicReference<String> currentArrayPathRef = new AtomicReference<>("");
        ObjectNode transformedPayload = objectMapper.createObjectNode();
//        System.out.println("\n--------------------------------------------\nmapping rules: "+allRules );

        for (Document mappingRule : allRules) {
            String sourceField = mappingRule.getString("source_field");
            String targetField = mappingRule.getString("target_field");
            boolean isKeyVal = mappingRule.getBoolean("isKeyVal");
            boolean belongsToArray = mappingRule.getBoolean("belongsToArray");
            boolean isArray = mappingRule.getBoolean("isArray");
            String script = mappingRule.getString("script");
            Object defaultValue = mappingRule.get("defaultValue");

            if(!isMappingRuleSuitable(payloadJsonCopy,sourceField,belongsToArray)){
                emitter.send("Mapping rule not suitable for source field: " + sourceField);
                break;
            }


            if (isKeyVal) {
                handleKeyValMapping(objectMapper, payloadJson, transformedPayload, sourceField, targetField, script, defaultValue, payloadJsonCopy);
            } else if (belongsToArray) {
                handleArrayMapping(objectMapper, payloadJson, transformedPayload, sourceField, targetField, script , defaultValue, currentArrayPathRef, payloadJsonCopy);
            } else {
                handleStandardMapping(objectMapper, payloadJson, transformedPayload, sourceField, targetField, isArray, script , defaultValue);
            }
        }
        logger.infof("Transformed payload: %s", transformedPayload);
        consumerRepository.addpayload(transformedPayload);
        emitter.send(objectMapper.writeValueAsString(transformedPayload));
        return transformedPayload;}
        catch (Exception e){
            e.printStackTrace();
            emitter.send("Exception occurred during mapping");
            return null;
        }

    }

    public void handleKeyValMapping(ObjectMapper objectMapper, JsonNode payloadJson, ObjectNode transformedPayload, String sourceField, String targetField, String script, Object defaultValue, JsonNode payloadJsonCopy) {
        String[] targetPathComponents = targetField.split("/");
        String key = targetPathComponents[targetPathComponents.length - 3];
        String keyName = targetPathComponents[targetPathComponents.length - 2];
        String valName = targetPathComponents[targetPathComponents.length - 1];
        JsonNode value;
        if (defaultValue != null) {
            value = objectMapper.valueToTree(defaultValue);
        } else if (script == null) {
            value = payloadJson.at(sourceField);
        } else {
            try {
                Object result = executeScript(script, payloadJsonCopy);
                if (result instanceof String) {
                    value = new TextNode((String) result);
                } else {
                    value = objectMapper.valueToTree(result);
                }
            } catch (ScriptException e) {
                e.printStackTrace();
                return;
            }
        }

        ObjectNode targetJson = objectMapper.createObjectNode();
        targetJson.put(keyName, key);
        targetJson.set(valName, value);
        ObjectNode currentNode = navigateToNode(objectMapper, transformedPayload, targetPathComponents, targetPathComponents.length - 4);

        if (currentNode.has(targetPathComponents[targetPathComponents.length - 4])) {
            arrayKeyVal = (ArrayNode) currentNode.get(targetPathComponents[targetPathComponents.length - 4]);
        } else {
            arrayKeyVal = objectMapper.createArrayNode();
        }
        arrayKeyVal.add(targetJson);
        currentNode.set(targetPathComponents[targetPathComponents.length - 4], arrayKeyVal);
    }

    public void handleArrayMapping(ObjectMapper objectMapper, JsonNode payloadJson, ObjectNode transformedPayload, String sourceField, String targetField, String script, Object defaultValue, AtomicReference<String> currentArrayPathRef, JsonNode payloadJsonCopy) {
        String fieldName = sourceField.split("/")[sourceField.split("/").length - 1];
        String targetFieldName = targetField.split("/")[targetField.split("/").length - 1];
        String arrayPath = sourceField.substring(0, sourceField.lastIndexOf('/'));
        boolean isNewArray = false;
        ArrayNode array = (ArrayNode) payloadJson.at(arrayPath);
        ArrayNode arrayCopy = (ArrayNode) payloadJsonCopy.at(arrayPath);
        // here im keeping track if the array path has changed
        if (!currentArrayPathRef.get().equals(arrayPath)) {
            currentArrayPathRef.set(arrayPath);
            isNewArray = true;
        }

        ArrayNode newArray = objectMapper.createArrayNode();

        if (fieldName.equals("*")) {
            newArray.addAll(array);
        } else {
            for (int i = 0; i < array.size(); i++) {
                ObjectNode currentNode = (ObjectNode) array.get(i);
                ObjectNode currentNodeCopy = (ObjectNode) arrayCopy.get(i);

                if (isNewArray) {
                    currentNode.removeAll();
                }

                JsonNode value;
                if (defaultValue != null) {
                    value = objectMapper.valueToTree(defaultValue);
                } else if (script != null) {
                    try {
                        Object result = executeScript(script, currentNodeCopy);
                        if (result instanceof String) {
                            value = new TextNode((String) result);
                        } else {
                            value = objectMapper.valueToTree(result);
                        }
                    } catch (ScriptException e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    value = currentNodeCopy.get(fieldName);
                }

                currentNode.remove(fieldName);
                currentNode.set(targetFieldName, value);
                newArray.add(currentNode);
            }
        }

        String[] targetPathComponents = targetField.substring(0, targetField.lastIndexOf('/')).split("/");
        ObjectNode navigatedNode = navigateToNode(objectMapper, transformedPayload, targetPathComponents, targetPathComponents.length - 1);
        navigatedNode.set(targetPathComponents[targetPathComponents.length - 1], newArray);
    }
    public void handleStandardMapping(ObjectMapper objectMapper, JsonNode payloadJson, ObjectNode transformedPayload, String sourceField, String targetField, boolean isArray, String script, Object defaultValue) {
        String[] targetPathComponents = targetField.split("/");
        ObjectNode currentNode = navigateToNode(objectMapper, transformedPayload, targetPathComponents, targetPathComponents.length - 1);

        if (isArray) {
            currentNode.set(targetPathComponents[targetPathComponents.length - 1], objectMapper.createArrayNode());
        } else {
            if( defaultValue != null){
                currentNode.set(targetPathComponents[targetPathComponents.length - 1],  objectMapper.valueToTree(defaultValue));
            }
            else if (script != null){

                try {
                    JsonNode value;
//                System.out.println("\n--------------------------------------------------\nscript : "+script);
                    Object result = executeScript(script, payloadJson);
//                System.out.println("\n----------------------------------------------\n payloadScript : "+payloadJson);
                    if (result instanceof String) {
                        value = new TextNode((String) result);
                    } else {
                        value = objectMapper.valueToTree(result);
                    }
                    currentNode.set(targetPathComponents[targetPathComponents.length - 1], value);
//                System.out.println("\nscript value : "+value);
                } catch (ScriptException e) {
                    e.printStackTrace();
                    return;
                }
            }
            else {
                currentNode.set(targetPathComponents[targetPathComponents.length - 1], payloadJson.at(sourceField));
            }
        }
    }

    public ObjectNode navigateToNode(ObjectMapper objectMapper, ObjectNode rootNode, String[] pathComponents, int endIndex) {
    AtomicReference<ObjectNode> currentNode = new AtomicReference<>(rootNode);
    IntStream.range(1, endIndex).forEach(i -> {
        String pathComponent = pathComponents[i];
        if (!currentNode.get().has(pathComponent)) {
            currentNode.get().set(pathComponent, objectMapper.createObjectNode());
        }
        currentNode.set((ObjectNode) currentNode.get().get(pathComponent));
    });
    return currentNode.get();
}

    public List<Document> getAllMappingRules(String collectionName) {
        System.out.println("collection name function: "+collectionName);

        return  mappingRuleService.getAllMappingRules(collectionName);
    }

    public Object executeScript(String script, JsonNode payload) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        engine.put("payload", payload);
        return engine.eval(script);
    }

    boolean isMappingRuleSuitable(JsonNode payloadJson, String sourceField, boolean belongsToArray) {
        if(sourceField.endsWith("*") || sourceField.isEmpty() || belongsToArray){
            return true;
        }
        return !payloadJson.at(sourceField).isMissingNode();

    }



}