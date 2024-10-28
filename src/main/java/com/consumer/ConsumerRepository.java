package com.consumer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;

@ApplicationScoped
public class ConsumerRepository {
    @Inject
    MongoClient mongoClient;

    public void addpayload(ObjectNode payload) {
        MongoDatabase database = mongoClient.getDatabase("test_db");
        MongoCollection<Document> collection = database.getCollection("mapping_result");
        Document document = Document.parse(payload.toString());
        collection.insertOne(document);
    }

}
