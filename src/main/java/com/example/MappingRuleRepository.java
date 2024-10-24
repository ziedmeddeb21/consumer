package com.example;

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
        Document document = new Document()
                .append("field_type", mappingRule.field_type)
                .append("source_field", mappingRule.source_field)
                .append("target_field", mappingRule.target_field)
                .append("isArray", mappingRule.isArray)
                        .append("isKeyVal", mappingRule.isKeyVal);


        collection.insertOne(document);
    }

    public List<Document> getMappingRule() {
        // Get all mapping rules
        MongoDatabase database = mongoClient.getDatabase("test_db");
        MongoCollection<Document> collection = database.getCollection("mapping_rules");
        return collection.find().into(new ArrayList<>());
    }

}
