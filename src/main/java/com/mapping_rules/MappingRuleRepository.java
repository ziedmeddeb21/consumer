package com.mapping_rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MappingRuleRepository {
    @Inject
    MongoClient mongoClient;

    public void addMappingRule(MappingRule mappingRule) {
        // Add a new mapping rule
        MongoDatabase database = mongoClient.getDatabase("test_db");
        MongoCollection<Document> collection = database.getCollection("mapping_rules");

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.convertValue(mappingRule, ObjectNode.class);
        Document document = Document.parse(objectNode.toString());

        collection.insertOne(document);
    }

    public List<Document> getMappingRule() {
        // Get all mapping rules
        MongoDatabase database = mongoClient.getDatabase("test_db");
        MongoCollection<Document> collection = database.getCollection("mapping_rules");
        return collection.find().into(new ArrayList<>());
    }

}
