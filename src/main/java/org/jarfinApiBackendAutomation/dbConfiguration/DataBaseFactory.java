package org.jarfinApiBackendAutomation.dbConfiguration;

import static org.jarfinApiBackendAutomation.dbConfiguration.DBConstants.JARFIN_MONGO_DB_URL;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBaseFactory {

    private static volatile MongoDBUtils jarFinMongo;

    public static synchronized void initJarFinMongoDB(String uri) {
        if (jarFinMongo == null) {
            jarFinMongo = new MongoDBUtils(uri);
            log.info("[DB Init] jarFin Mongo initialized");
        }
        jarFinMongo.initializeClient();
    }

    public static MongoDBUtils jarFinMongo() {
        return jarFinMongo;
    }

    public static void closeAllDBConnections() {
        if (jarFinMongo != null) jarFinMongo.closeConnection();
        log.info("[DB Close] All database connections closed");
    }

    public static void initializeDBConnections(String dbServer) {

        if (dbServer == null || dbServer.isBlank()) {
            dbServer = "jarfin";
            log.info("No DB server specified. Defaulting to 'jarfin'.");
        }

        switch (dbServer.toLowerCase()) {
            case "jarfin":
                initJarFinMongoDB(JARFIN_MONGO_DB_URL);
                break;
            default:
                log.warn(
                        "Unknown DB server specified: {}. No DB connections initialized.",
                        dbServer);
        }
    }
}
