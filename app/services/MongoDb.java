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

import java.util.Arrays;

public class MongoDb {
    private static final int TIMEOUT_CONNECT = 15 * 1000; // 15 seconds

    private final MongoClient mongoClient;
    private final MongoDatabase db;
    private final Morphia morphia;
    private final Datastore ds;

    @Inject
    public MongoDb(ActorSystem system) {
        // Don't use TLS by default for local development environments and for MongoDBs in OpenShift containers
        boolean tls = false;
        String hostname = "localhost";
        String username = null;
        String password = null;
        String database = "railinfo-ch";
        MongoClientOptions options = MongoClientOptions.builder().connectTimeout(TIMEOUT_CONNECT).sslEnabled(tls).build();
        if (username != null && password != null) {
            MongoCredential credential = MongoCredential.createCredential(username, database, password.toCharArray());
            new ServerAddress(hostname);
            mongoClient = new MongoClient(new ServerAddress(hostname), Arrays.asList(credential), options);
        } else {
            mongoClient = new MongoClient(hostname, options);
        }

        db = this.mongoClient.getDatabase(database);

        morphia = new Morphia();
        morphia.map(Edge.class);
        morphia.map(Stop.class);
        morphia.map(StopTime.class);
        morphia.map(Trip.class);
        morphia.map(Route.class);
        morphia.map(ServiceCalendar.class);
        morphia.map(ServiceCalendarException.class);

        ds = morphia.createDatastore(mongoClient, database);
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
