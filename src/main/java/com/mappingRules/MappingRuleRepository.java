package com.mappingRules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MappingRuleRepository {
    @Inject
    MongoClient mongoClient;
    private final Logger logger = Logger.getLogger(MappingRuleRepository.class);

    private MongoDatabase getDatabase() {
        return mongoClient.getDatabase("test_db");
    }

    private MongoCollection<Document> getCollection(String collectionName) {
        return getDatabase().getCollection(collectionName);
    }

    public Document addMappingRule(MappingRule mappingRule, String collectionName) {
        MongoCollection<Document> collection = getCollection(collectionName);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.convertValue(mappingRule, ObjectNode.class);
        Document document = Document.parse(objectNode.toString());

        collection.insertOne(document);
        return document;
    }

    public List<Document> getMappingRule(String collectionName) {
        MongoCollection<Document> collection = getCollection(collectionName);

        return collection.aggregate(List.of(
                new Document("$addFields", new Document("_id", new Document("$toString", "$_id")))
        )).into(new ArrayList<>());
    }

    public List<String> getCollectionNames() {
        return getDatabase().listCollectionNames().into(new ArrayList<>());
    }

    public Document getMappingRuleById(String collectionName, String id) {
        MongoCollection<Document> collection = getCollection(collectionName);
        ObjectId objectId = new ObjectId(id);

        return getDocument(collection, objectId);
    }

    private Document getDocument(MongoCollection<Document> collection, ObjectId objectId) {
        List<Document> result = collection.aggregate(List.of(
                new Document("$match", new Document("_id", objectId)),
                new Document("$addFields", new Document("_id", new Document("$toString", "$_id")))
        )).into(new ArrayList<>());

        return result.isEmpty() ? null : result.get(0);
    }

    public Document updateMappingRule(String id, MappingRule mappingRule, String collectionName) {
        MongoCollection<Document> collection = getCollection(collectionName);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.convertValue(mappingRule, ObjectNode.class);
        Document document = Document.parse(objectNode.toString());

        ObjectId objectId = new ObjectId(id);
        collection.replaceOne(new Document("_id", objectId), document);

        return getDocument(collection, objectId);
    }

    public void deleteMappingRule(String id, String collectionName) {
        MongoCollection<Document> collection = getCollection(collectionName);
        ObjectId objectId = new ObjectId(id);
        collection.deleteOne(new Document("_id", objectId));
    }
}
