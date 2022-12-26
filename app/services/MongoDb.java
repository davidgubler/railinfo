package services;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import entities.*;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import entities.mongodb.MongoDbEdge;
import entities.mongodb.MongoDbUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MongoDb {
    private static final int TIMEOUT_CONNECT = 15 * 1000; // 15 seconds

    private final Map<String, DatabaseConnection> connections = new HashMap<>();

    private final ActorSystem actorSystem;

    private void connect(String databaseName) {
        // Don't use TLS by default for local development environments and for MongoDBs in OpenShift containers
        boolean tls = false;
        String hostname = "localhost";
        String username = null;
        String password = null;
        MongoClient mongoClient;
        Morphia morphia;
        Datastore ds;
        MongoClientOptions options = MongoClientOptions.builder().connectTimeout(TIMEOUT_CONNECT).sslEnabled(tls).build();
        if (username != null && password != null) {
            MongoCredential credential = MongoCredential.createCredential(username, databaseName, password.toCharArray());
            new ServerAddress(hostname);
            mongoClient = new MongoClient(new ServerAddress(hostname), Arrays.asList(credential), options);
        } else {
            mongoClient = new MongoClient(hostname, options);
        }

        MongoDatabase db = mongoClient.getDatabase(databaseName);

        morphia = new Morphia();
        if (!databaseName.contains("-")) {
            // it is the global database
            morphia.map(MongoDbUser.class);
        } else {
            // it is a timetable database
            morphia.map(MongoDbEdge.class);
            morphia.map(Stop.class);
            morphia.map(StopTime.class);
            morphia.map(Trip.class);
            morphia.map(Route.class);
            morphia.map(ServiceCalendar.class);
            morphia.map(ServiceCalendarException.class);
        }

        ds = morphia.createDatastore(mongoClient, databaseName);
        ds.ensureIndexes();
        ds.ensureCaps();

        actorSystem.registerOnTermination(() -> mongoClient.close());

        connections.put(databaseName, new DatabaseConnection(mongoClient, db, morphia, ds));
    }

    @Inject
    public MongoDb(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public MongoDatabase get(String databaseName) {
        if (!connections.containsKey(databaseName)) {
            connect(databaseName);
        }
        return connections.get(databaseName).db;
    }

    public Datastore getDs(String databaseName) {
        if (!connections.containsKey(databaseName)) {
            connect(databaseName);
        }
        return connections.get(databaseName).ds;
    }

    private class DatabaseConnection {
        DatabaseConnection(MongoClient mongoClient, MongoDatabase db, Morphia morphia, Datastore ds) {
            this.mongoClient = mongoClient;
            this.db = db;
            this.morphia = morphia;
            this.ds = ds;
        }
        final MongoClient mongoClient;
        final MongoDatabase db;
        final Morphia morphia;
        final Datastore ds;
    }
}
