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
    ArrayNode arrayKeyVal;

    @Incoming("json-in")
    public void receive(String payload) throws JsonProcessingException {
        mapPayload(payload, getAllMappingRules());

    }

    public ObjectNode mapPayload(String payload ,List<Document> allRules) throws JsonProcessingException {
//        System.out.println("Payload: " + payload);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadJson = objectMapper.readTree(payload);
        // i used this because the original payload can be modified
        JsonNode payloadJsonCopy = payloadJson.deepCopy();
        ObjectNode transformedPayload = objectMapper.createObjectNode();


        for (Document mappingRule : allRules) {
            String sourceField = mappingRule.getString("source_field");
            String targetField = mappingRule.getString("target_field");
            boolean isKeyVal = mappingRule.getBoolean("isKeyVal");
            boolean belongsToArray = mappingRule.getBoolean("belongsToArray");
            boolean isArray = mappingRule.getBoolean("isArray");
            String script = mappingRule.getString("script");
            Object defaultValue = mappingRule.get("defaultValue");
            if (isKeyVal) {
                handleKeyValMapping(objectMapper, payloadJson, transformedPayload, sourceField, targetField, script, defaultValue, payloadJsonCopy);
            } else if (belongsToArray) {
                handleArrayMapping(objectMapper, payloadJson, transformedPayload, sourceField, targetField, script);
            } else {
                handleStandardMapping(objectMapper, payloadJson, transformedPayload, sourceField, targetField, isArray, script);
            }
        }
        logger.infof("Transformed payload: %s", transformedPayload);
        consumerRepository.addpayload(transformedPayload);
        return transformedPayload;

    }

    private void handleKeyValMapping(ObjectMapper objectMapper, JsonNode payloadJson, ObjectNode transformedPayload, String sourceField, String targetField, String script,Object defaultValue , JsonNode payloadJsonCopy) {
        String[] targetPathComponents = targetField.split("/");
        String key = targetPathComponents[targetPathComponents.length - 3];
        String keyName = targetPathComponents[targetPathComponents.length - 2];
        String valName = targetPathComponents[targetPathComponents.length - 1];
        JsonNode value;
        if(defaultValue != null){
            value = objectMapper.valueToTree(defaultValue);
        }
        else if(script == null){
            value = payloadJson.at(sourceField);

        }
        else{
            try {
                System.out.println("\n--------------------------------------------------\nscript : "+script);
                Object result = executeScript(script, payloadJsonCopy);
//                System.out.println("\n----------------------------------------------\n payloadScript : "+payloadJson);
                if (result instanceof String) {
                    value = new TextNode((String) result);
                } else {
                    value = objectMapper.valueToTree(result);
                }
                System.out.println("\nscript value : "+value);
            } catch (ScriptException e) {
                e.printStackTrace();
                return;
            }
        }


        ObjectNode targetJson = objectMapper.createObjectNode();
        targetJson.put(keyName, key);
        targetJson.set(valName, value);
        ObjectNode currentNode = navigateToNode(objectMapper, transformedPayload, targetPathComponents, targetPathComponents.length - 4);

        //exp /inf/metadata(1)/motifOperation(2)/field_code(3)/field_value(4)     length=5-3=2  5-4=1
        if (currentNode.has(targetPathComponents[targetPathComponents.length - 4])) {
            arrayKeyVal.add(targetJson);
        }
        else {
            arrayKeyVal = objectMapper.createArrayNode();
            arrayKeyVal.add(targetJson);

        }

        currentNode.set(targetPathComponents[targetPathComponents.length - 4], arrayKeyVal);

    }

    private void handleArrayMapping(ObjectMapper objectMapper, JsonNode payloadJson, ObjectNode transformedPayload, String sourceField, String targetField, String script) {
        String fieldName = sourceField.split("/")[sourceField.split("/").length - 1];
        String targetFieldName = targetField.split("/")[targetField.split("/").length - 1];
        String arrayPath = sourceField.substring(0, sourceField.lastIndexOf('/'));
        ArrayNode array = (ArrayNode) payloadJson.at(arrayPath);



        ArrayNode newArray = objectMapper.createArrayNode();
//        System.out.println("array path : "+arrayPath);
//        System.out.println("array : "+array);
        //  cas particulier pour les listes qui ont des simples strings
        if(fieldName.equals("*")){
            newArray.addAll(array);
        }
        else{

            for (JsonNode node : array) {
                ObjectNode currentNode = (ObjectNode) node;
                JsonNode value = currentNode.get(fieldName);
                currentNode.remove(fieldName);
                currentNode.set(targetFieldName, value);
                newArray.add(currentNode);
            }


        }

// auth keyclaok
        // menu parametrage gere les mapping rules
        // input output;

        String[] targetPathComponents = targetField.substring(0, targetField.lastIndexOf('/')).split("/");
        ObjectNode navigatedNode = navigateToNode(objectMapper, transformedPayload, targetPathComponents, targetPathComponents.length - 1);
        navigatedNode.set(targetPathComponents[targetPathComponents.length - 1], newArray);
    }

    private void handleStandardMapping(ObjectMapper objectMapper, JsonNode payloadJson, ObjectNode transformedPayload, String sourceField, String targetField, boolean isArray, String script) {
        String[] targetPathComponents = targetField.split("/");
        ObjectNode currentNode = navigateToNode(objectMapper, transformedPayload, targetPathComponents, targetPathComponents.length - 1);

        if (isArray) {
            currentNode.set(targetPathComponents[targetPathComponents.length - 1], objectMapper.createArrayNode());
        } else {
            currentNode.set(targetPathComponents[targetPathComponents.length - 1], payloadJson.at(sourceField));
        }
    }

    private ObjectNode navigateToNode(ObjectMapper objectMapper, ObjectNode rootNode, String[] pathComponents, int endIndex) {
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

    private List<Document> getAllMappingRules() {
        return mappingRuleService.getAllMappingRules();
    }

    public Object executeScript(String script, JsonNode payload) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        engine.put("payload", payload);
        return engine.eval(script);
    }





}