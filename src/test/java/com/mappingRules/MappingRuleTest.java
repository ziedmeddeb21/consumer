package com.mappingRules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.netty.util.Mapping;
import io.quarkus.test.junit.QuarkusTest;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@QuarkusTest

public class MappingRuleTest  {


    @InjectMocks
    private MappingRuleService mappingService;

    @Mock
    private MappingRuleRepository mappingRuleRepository;

    @Mock
    MongoClient mongoClient;  // Mock MongoClient

    @Mock
    MongoDatabase mongoDatabase;  // Mock MongoDatabase

    @Mock
    MongoCollection<Document> mongoCollection;  // Mock MongoCollection

    @Mock
    FindIterable<Document> findIterable;



    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mongoClient.getDatabase("test_db")).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection("mapping_rules")).thenReturn(mongoCollection);
        mappingRuleRepository.mongoClient = mongoClient;

    }


    @Test
    void testGetMappingRule() {
        Document doc1 = new Document("key1", "value1");
        Document doc2 = new Document("key2", "value2");
        List<Document> mockDocuments = new ArrayList<>();
        mockDocuments.add(doc1);
        mockDocuments.add(doc2);

        // Mock the findIterable to return the mock documents
        when(mongoCollection.find()).thenReturn(findIterable);
        when(findIterable.into(any(List.class))).thenAnswer(invocation -> {
            List<Document> list = invocation.getArgument(0);
            list.addAll(mockDocuments);
            return list;
        });

        when(mappingRuleRepository.getMappingRule()).thenCallRealMethod();

        List<Document> findAllResult = mappingService.getAllMappingRules();

        assertEquals(2, findAllResult.size());
        assertEquals(doc1, findAllResult.get(0));
        assertEquals(doc2, findAllResult.get(1));

        verify(mappingRuleRepository, times(1)).getMappingRule();
        verify(mongoCollection, times(1)).find();
    }


    @Test
    public void testCreateMappingRule() throws Exception {
        MappingRule mockRule = new MappingRule(true, "source", "target", false, false);
        ObjectMapper mapper = new ObjectMapper();

        Mockito.when(mappingRuleRepository.addMappingRule(Mockito.any(MappingRule.class))).thenAnswer(invocation -> {
            Document doc = Document.parse(mapper.writeValueAsString(invocation.getArgument(0)));
            mongoCollection.insertOne(doc);
            return doc;
        });

         mappingService.createMappingRule(mockRule);

        verify(mongoCollection, times(1)).insertOne(any(Document.class));
    }


}
