package com.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mappingRules.MappingRuleService;
import io.quarkus.test.junit.QuarkusTest;
import org.bson.Document;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class ConsumerTest {
//    private ScriptEngine engine;
//    @InjectMocks
//    ConsumerService consumerService;
//
//    @Mock
//    ConsumerRepository consumerRepository;
//
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//        ScriptEngineManager manager = new ScriptEngineManager();
//        engine = manager.getEngineByName("nashorn");
//
//    }

//    @Test
//    public void testMapPayload() throws Exception {
//        String json = "{\"name\":\"test\",\"value\":123,\"messages\":[{\"from\":\"zied\",\"to\":\"iheb\",\"message\":\"hello\"},{\"from\":\"iheb\",\"to\":\"zied\",\"message\":\"wassup\"}],\"metadata\":{\"title\":\"Batman\",\"year\":2013}}";
//        List<Document> allMappingRules = new ArrayList<>();
//        allMappingRules.add(new Document("source_field", "/name")
//                .append("target_field", "/nom")
//                .append("isKeyVal", false)
//                .append("belongsToArray", false)
//                .append("isArray", false));
//        allMappingRules.add(new Document("source_field", "/value")
//                .append("target_field", "/val")
//                .append("isKeyVal", false)
//                .append("belongsToArray", false)
//                .append("isArray", false));
//        allMappingRules.add(new Document("source_field", "/messages")
//                .append("target_field", "/msgs")
//                .append("isKeyVal", false)
//                .append("belongsToArray", false)
//                .append("isArray", true));
//        allMappingRules.add(new Document("source_field", "/messages/from")
//                .append("target_field", "/msgs/de")
//                .append("isKeyVal", false)
//                .append("belongsToArray", true)
//                .append("isArray", false));
//        allMappingRules.add(new Document("source_field", "/messages/to")
//                .append("target_field", "/msgs/a")
//                .append("isKeyVal", false)
//                .append("belongsToArray", true)
//                .append("isArray", false));
//        allMappingRules.add(new Document("source_field", "/messages/message")
//                .append("target_field", "/msgs/msg")
//                .append("isKeyVal", false)
//                .append("belongsToArray", true)
//                .append("isArray", false));
//        allMappingRules.add(new Document("source_field", "/metadata/title")
//                .append("target_field", "/iinfosss/infos/titre/modKey/modVal")
//                .append("isKeyVal", true)
//                .append("belongsToArray", false)
//                .append("isArray", false));
//        allMappingRules.add(new Document("source_field", "/metadata/year")
//                .append("target_field", "/inf/annee")
//                .append("isKeyVal", false)
//                .append("belongsToArray", false)
//                .append("isArray", false));
//
//        ObjectNode transformedPayload = consumerService.mapPayload(json, allMappingRules);
//
//        assertTrue(transformedPayload.has("nom"));
//        assertTrue(transformedPayload.get("nom").asText().equals("test"));
//        assertTrue(transformedPayload.has("val"));
//        assertTrue(transformedPayload.get("val").asInt() == 123);
//        assertTrue(transformedPayload.has("msgs"));
//        assertTrue(transformedPayload.get("msgs").isArray());
//        JsonNode messages = transformedPayload.get("msgs");
//        assertTrue(messages.get(0).has("de"));
//        assertTrue(messages.get(0).get("de").asText().equals("zied"));
//        assertTrue(messages.get(0).has("a"));
//        assertTrue(messages.get(0).get("a").asText().equals("iheb"));
//        assertTrue(messages.get(0).has("msg"));
//        assertTrue(messages.get(0).get("msg").asText().equals("hello"));
//        assertTrue(messages.get(1).has("de"));
//        assertTrue(messages.get(1).get("de").asText().equals("iheb"));
//        assertTrue(messages.get(1).has("a"));
//        assertTrue(messages.get(1).get("a").asText().equals("zied"));
//        assertTrue(messages.get(1).has("msg"));
//        assertTrue(messages.get(1).get("msg").asText().equals("wassup"));
//        assertTrue(transformedPayload.has("iinfosss"));
//        assertTrue(transformedPayload.get("iinfosss").has("infos"));
//        assertTrue(transformedPayload.get("iinfosss").get("infos").isArray());
//        JsonNode infos = transformedPayload.get("iinfosss").get("infos");
//        assertTrue(infos.get(0).has("modKey"));
//        assertTrue(infos.get(0).get("modKey").asText().equals("titre"));
//        assertTrue(infos.get(0).has("modVal"));
//        assertTrue(transformedPayload.has("inf"));
//        assertTrue(transformedPayload.get("inf").has("annee"));
//        verify(consumerRepository, times(1)).addpayload(any());
//    }

//    @Test
//    public void testNashornScript() throws ScriptException {
//        String script = "var payload = '{\"name\":\"John\", \"age\":30}';" +
//                "var transformedPayload = JSON.parse(payload);" +
//                "transformedPayload.name = 'Jane';" +
//                "transformedPayload.age = 25;" +
//                "JSON.stringify(transformedPayload);";
//
//        Object result = engine.eval(script);
//        String expected = "{\"name\":\"Jane\",\"age\":25}";
//
//        assertEquals(expected, result);
//    }

    @InjectMocks
    ConsumerService consumerService;

    @Mock
    ConsumerRepository consumerRepository;

    @Mock
    MappingRuleService mappingRuleService;

    @Mock
    Emitter<String> emitter;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

//    @Test
//    void testReceive_shouldMapPayloadCorrectly() throws Exception {
//        // Arrange: Prepare the mock input JSON and mapping rules
//        String inputJson = "{\"content\": {\"field1\": \"value1\"}, \"collectionName\": \"collection1\"}";
//        JsonNode inputPayload = objectMapper.readTree(inputJson);
//        List<Document> mappingRules = List.of(createMappingRule("field1", "mappedField1"));
//
//        when(mappingRuleService.getAllMappingRules("collection1")).thenReturn(mappingRules);
//
//        // Act: Call the method under test
//        consumerService.receive(inputJson);
//
//        // Assert: Verify the emitter and repository interactions
//        verify(emitter).send(anyString()); // Ensure transformed payload is emitted
//        verify(consumerRepository).addpayload(any()); // Ensure the transformed payload is saved
//    }

//    private Document createMappingRule(String sourceField, String targetField) {
//        Document rule = new Document();
//        rule.put("source_field", sourceField);
//        rule.put("target_field", targetField);
//        rule.put("isKeyVal", false);
//        rule.put("belongsToArray", false);
//        rule.put("isArray", false);
//        rule.put("script", null);
//        rule.put("defaultValue", null);
//        return rule;
//    }

//    @Test
//    void testMapPayload_withKeyValMapping() throws Exception {
//        // Arrange
//        JsonNode inputPayload = objectMapper.readTree("{\"field1\": \"value1\"}");
//        List<Document> mappingRules = List.of(createKeyValMappingRule());
//
//        // Act
//        ObjectNode result = consumerService.mapPayload(inputPayload, mappingRules);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("value1", result.get("mappedField1").asText());
//    }

//    private Document createKeyValMappingRule() {
//        Document rule = new Document();
//        rule.put("source_field", "/field1");
//        rule.put("target_field", "/mappedField1");
//        rule.put("isKeyVal", true);
//        rule.put("belongsToArray", false);
//        rule.put("isArray", false);
//        rule.put("script", null);
//        rule.put("defaultValue", null);
//        return rule;
//    }
//
//    @Test
//    void testHandleArrayMapping() throws Exception {
//        // Arrange: Prepare an array in the payload
//        String payloadJson = "{\"arrayField\": [{\"item\": \"value1\"}, {\"item\": \"value2\"}]}";
//        JsonNode inputPayload = objectMapper.readTree(payloadJson);
//        List<Document> mappingRules = List.of(createArrayMappingRule());
//
//        // Act: Call handleArrayMapping
//        consumerService.handleArrayMapping(objectMapper, inputPayload, objectMapper.createObjectNode(), "/arrayField", "/arrayField", null, null, new AtomicReference<>(), inputPayload);
//
//        // Assert: Verify array transformation
//        // Here you would add assertions specific to the structure of the transformed payload.
//    }
//
//    private Document createArrayMappingRule() {
//        Document rule = new Document();
//        rule.put("source_field", "/arrayField/item");
//        rule.put("target_field", "/arrayField/mappedItem");
//        rule.put("isKeyVal", false);
//        rule.put("belongsToArray", true);
//        rule.put("isArray", false);
//        rule.put("script", null);
//        rule.put("defaultValue", null);
//        return rule;
//    }
//
//    @Test
//    void testExecuteScript() throws ScriptException, JsonProcessingException {
//        // Arrange
//        String script = "payload.get(\"field1\").asText()";
//        JsonNode inputPayload = objectMapper.readTree("{\"field1\": \"value1\"}");
//
//        // Act
//        Object result = consumerService.executeScript(script, inputPayload);
//
//        // Assert
//        assertEquals("value1", result);
//    }
//
//    @Test
//    void testIsMappingRuleSuitable() throws JsonProcessingException {
//        // Arrange
//        JsonNode inputPayload = objectMapper.readTree("{\"field1\": \"value1\"}");
//
//        // Act and Assert
//        assertTrue(consumerService.isMappingRuleSuitable(inputPayload, "/field1", false)); // Valid field
//        assertFalse(consumerService.isMappingRuleSuitable(inputPayload, "/missingField", false)); // Invalid field
//        assertTrue(consumerService.isMappingRuleSuitable(inputPayload, "/field1", true)); // Array check
//    }



}