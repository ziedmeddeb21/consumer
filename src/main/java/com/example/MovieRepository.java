package com.example;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;

@ApplicationScoped
public class MovieRepository {
    @Inject
    MongoClient mongoClient;

    public void addMovie(ObjectNode movie) {
        MongoDatabase database = mongoClient.getDatabase("test_db");
        MongoCollection<Document> collection = database.getCollection("movies");
        Document document = Document.parse(movie.toString());
        collection.insertOne(document);
    }

}
