package com.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapping_rules.MappingRuleService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@QuarkusTest
public class ConsumerTest {

    @InjectMocks
    ConsumerService consumerService;

    @Mock
    ConsumerRepository consumerRepository;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testMapPayload() throws Exception {
        // Arrange
        String json = "{\"name\":\"test\",\"value\":123,\"messages\":[{\"from\":\"zied\",\"to\":\"iheb\",\"message\":\"hello\"},{\"from\":\"iheb\",\"to\":\"zied\",\"message\":\"wassup\"}],\"metadata\":{\"title\":\"Batman\",\"year\":2013}}";
        List<Document> allMappingRules = new ArrayList<>();
        allMappingRules.add(new Document("source_field", "/name")
                .append("target_field", "/nom")
                .append("isKeyVal", false)
                .append("belongsToArray", false)
                .append("isArray", false));
        allMappingRules.add(new Document("source_field", "/value")
                .append("target_field", "/val")
                .append("isKeyVal", false)
                .append("belongsToArray", false)
                .append("isArray", false));
        allMappingRules.add(new Document("source_field", "/messages")
                .append("target_field", "/msgs")
                .append("isKeyVal", false)
                .append("belongsToArray", false)
                .append("isArray", true));
        allMappingRules.add(new Document("source_field", "/messages/from")
                .append("target_field", "/msgs/de")
                .append("isKeyVal", false)
                .append("belongsToArray", true)
                .append("isArray", false));
        allMappingRules.add(new Document("source_field", "/messages/to")
                .append("target_field", "/msgs/a")
                .append("isKeyVal", false)
                .append("belongsToArray", true)
                .append("isArray", false));
        allMappingRules.add(new Document("source_field", "/messages/message")
                .append("target_field", "/msgs/msg")
                .append("isKeyVal", false)
                .append("belongsToArray", true)
                .append("isArray", false));
        allMappingRules.add(new Document("source_field", "/metadata/title")
                .append("target_field", "/iinfosss/infos/titre/modKey/modVal")
                .append("isKeyVal", true)
                .append("belongsToArray", false)
                .append("isArray", false));
        allMappingRules.add(new Document("source_field", "/metadata/year")
                .append("target_field", "/inf/annee")
                .append("isKeyVal", false)
                .append("belongsToArray", false)
                .append("isArray", false));

        // Act
        ObjectNode transformedPayload = consumerService.mapPayload(json, allMappingRules);

        // Assert
        assertTrue(transformedPayload.has("nom"));
        assertTrue(transformedPayload.get("nom").asText().equals("test"));
        assertTrue(transformedPayload.has("val"));
        assertTrue(transformedPayload.get("val").asInt() == 123);
        assertTrue(transformedPayload.has("msgs"));
        assertTrue(transformedPayload.get("msgs").isArray());
        JsonNode messages = transformedPayload.get("msgs");
        assertTrue(messages.get(0).has("de"));
        assertTrue(messages.get(0).get("de").asText().equals("zied"));
        assertTrue(messages.get(0).has("a"));
        assertTrue(messages.get(0).get("a").asText().equals("iheb"));
        assertTrue(messages.get(0).has("msg"));
        assertTrue(messages.get(0).get("msg").asText().equals("hello"));
        assertTrue(messages.get(1).has("de"));
        assertTrue(messages.get(1).get("de").asText().equals("iheb"));
        assertTrue(messages.get(1).has("a"));
        assertTrue(messages.get(1).get("a").asText().equals("zied"));
        assertTrue(messages.get(1).has("msg"));
        assertTrue(messages.get(1).get("msg").asText().equals("wassup"));
        assertTrue(transformedPayload.has("iinfosss"));
        assertTrue(transformedPayload.get("iinfosss").has("infos"));
        assertTrue(transformedPayload.get("iinfosss").get("infos").isArray());
        JsonNode infos = transformedPayload.get("iinfosss").get("infos");
        assertTrue(infos.get(0).has("modKey"));
        assertTrue(infos.get(0).get("modKey").asText().equals("titre"));
        assertTrue(infos.get(0).has("modVal"));
        assertTrue(transformedPayload.has("inf"));
        assertTrue(transformedPayload.get("inf").has("annee"));
        verify(consumerRepository, times(1)).addpayload(any());
    }


}