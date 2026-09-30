package org.jarfinApiBackendAutomation.dbConfiguration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;

@Slf4j
public class MongoDBUtils {
    // Volatile to ensure thread-safety
    private volatile MongoClient mongoClient;
    private final String uri;

    public MongoDBUtils(String uri) {
        this.uri = uri;
    }

    /** Thread-safe, lazy Singleton */
    public MongoClient initializeClient() {
        if (mongoClient == null) {
            synchronized (this) {
                if (mongoClient == null) {
                    mongoClient =
                            MongoClients.create(
                                    MongoClientSettings.builder()
                                            .applyConnectionString(new ConnectionString(uri))
                                            .applyToSocketSettings(
                                                    builder ->
                                                            builder.connectTimeout(
                                                                    60, TimeUnit.SECONDS))
                                            .build());
                    log.info("MongoDB Client Initialized");
                }
            }
        }
        return mongoClient;
    }

    /** Returns MongoDatabase instance from the singleton client */
    public MongoDatabase getDatabase(String dbName) {
        return initializeClient().getDatabase(dbName);
    }

    /** Returns MongoCollection instance from the singleton client */
    public MongoCollection<Document> getCollection(String dbName, String collectionName) {
        return getDatabase(dbName).getCollection(collectionName);
    }

    /**
     * @param dbName Database name
     * @param collection Collection name
     * @param filterKey Field to filter on
     * @param value Value to match
     * @param sortField Field to sort by (descending)
     */
    public Document fetchData(
            String dbName, String collection, String filterKey, String value, String sortField) {
        try {
            MongoCollection<Document> dbCollection = getCollection(dbName, collection);
            Document query;
            if ("_id".equals(filterKey)) {
                query = new Document("_id", new ObjectId(value));
            } else {
                query = new Document(filterKey, value);
            }

            Document sort = new Document(sortField, -1);

            Document document =
                    dbCollection
                            .find(query)
                            .sort(sort)
                            .limit(1)
                            .first(); // returns first document or null if none found
            if (document != null) {
                log.info("Fetched Document: {}", document);
            } else {
                log.info("No Document found for Query: {}", query);
            }
            return document;
        } catch (Exception e) {
            log.info("Error while fetching data: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Document fetchDataMultiFilter(
            String dbName, String collection, Map<String, Object> filters, String sortField) {

        try {
            MongoCollection<Document> dbCollection = getCollection(dbName, collection);

            Document query = new Document();

            if (filters != null) {
                for (Map.Entry<String, Object> entry : filters.entrySet()) {
                    if (entry.getValue() == null) {
                        log.warn("Skipping null filter for key={}", entry.getKey());
                        continue;
                    }

                    if ("_id".equals(entry.getKey())) {
                        query.append("_id", new ObjectId(entry.getValue().toString()));
                    } else {
                        query.append(entry.getKey(), entry.getValue());
                    }
                }
            }

            FindIterable<Document> findIterable = dbCollection.find(query);

            if (sortField != null && !sortField.isBlank()) {
                findIterable = findIterable.sort(new Document(sortField, -1));
            }

            Document document = findIterable.first();

            if (document != null) {
                log.info("Fetched Document: {}", document.toJson());
            } else {
                log.info("No Document found for Query: {}", query.toJson());
            }

            return document;

        } catch (Exception e) {
            log.error("Error while fetching data", e);
            return null;
        }
    }

    public void insertDocument(String dbName, String collection, Document document) {
        try {
            getCollection(dbName, collection).insertOne(document);
            log.info("Inserted document into {}.{}: {}", dbName, collection, document.toJson());
        } catch (Exception e) {
            log.error(
                    "Error inserting document into {}.{}: {}", dbName, collection, e.getMessage());
        }
    }

    /** Close the MongoClient */
    public void closeConnection() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
                mongoClient = null;
                log.info("MongoDB connection closed.");
            }
        } catch (Exception e) {
            log.error("Error closing MongoDB connection: {}", e.getMessage());
        }
    }
}
