package services;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import entities.*;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

import java.util.Arrays;

public class MongoDb {
    private static final int TIMEOUT_CONNECT = 15 * 1000; // 15 seconds

    private final MongoClient mongoClient;
    private final MongoDatabase db;
    private final Datastore ds;

    @Inject
    public MongoDb(ActorSystem system) {
        // Don't use TLS by default for local development environments and for MongoDBs in OpenShift containers
        boolean tls = false;
        String hostname = "localhost";
        String username = null;
        String password = null;
        String database = "railinfo-ch2";
        MongoClientSettings.Builder settings = MongoClientSettings.builder();
        if (username != null && password != null) {
            MongoCredential credential = MongoCredential.createCredential(username, database, password.toCharArray());
            settings.credential(credential);
        }
        settings.applyToSslSettings(builder -> builder.enabled(true));
        settings.applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(hostname, 27017))));
        mongoClient = MongoClients.create(settings.build());
        db = mongoClient.getDatabase(database);
        ds = Morphia.createDatastore(mongoClient, database);
        ds.getMapper().map(Stop.class);
        ds.getMapper().map(StopTime.class);
        ds.getMapper().map(Trip.class);
        ds.getMapper().map(ServiceCalendar.class);
        ds.getMapper().map(ServiceCalendarException.class);
        ds.ensureIndexes();
        ds.ensureCaps();

        system.registerOnTermination(() -> {
            mongoClient.close();
        });
    }

    public MongoDatabase get() {
        return db;
    }

    public Datastore getDs() {
        return ds;
    }
}
