package services;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import configs.CH;
import configs.GtfsConfig;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import entities.mongodb.*;
import utils.Config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MongoDb {
    private static final int TIMEOUT_CONNECT = 15 * 1000; // 15 seconds

    private final Map<String, DatabaseConnection> connections = new ConcurrentHashMap<>();

    private MongoClient client;

    private final ActorSystem actorSystem;

    private MongoClient getClient() {
        if (client == null) {
            boolean tls = false;
            String hostname = "localhost";
            String username = null;
            String password = null;
            MongoClientOptions options = MongoClientOptions.builder().connectTimeout(TIMEOUT_CONNECT).sslEnabled(tls).build();
            if (username != null && password != null) {
                MongoCredential credential = MongoCredential.createCredential(username, Config.GLOBAL_DB, password.toCharArray());
                new ServerAddress(hostname);
                client = new MongoClient(new ServerAddress(hostname), Arrays.asList(credential), options);
            } else {
                client = new MongoClient(hostname, options);
            }
            actorSystem.registerOnTermination(() -> client.close());
        }
        return client;
    }

    public GtfsConfig getLatest(String countryCode) {
        countryCode = countryCode.toLowerCase();
        if ("ch".equals(countryCode)) {
            List<String> databaseNames = this.getTimetableDatabases(countryCode);
            if (!databaseNames.isEmpty()) {
                return new CH(get(databaseNames.get(0)), getDs(databaseNames.get(0)));
            }
        }
        return null;
    }

    public List<String> getTimetableDatabases(String countryCode) {
        List<String> databases = new LinkedList<>();
        Iterator<String> i = getClient().listDatabaseNames().iterator();
        while (i.hasNext()) {
            String dbName = i.next();
            if (dbName.startsWith("railinfo-" + countryCode + "-")) {
                databases.add(dbName);
            }
        }
        Collections.sort(databases);
        Collections.reverse(databases);
        return databases;
    }

    private void connect(String databaseName) {
        MongoClient client = getClient();
        Morphia morphia;
        Datastore ds;
        MongoDatabase db = client.getDatabase(databaseName);

        morphia = new Morphia();
        if (!databaseName.contains("-")) {
            // it is the global database
            morphia.map(MongoDbUser.class);
        } else {
            // it is a timetable database
            morphia.map(MongoDbEdge.class);
            morphia.map(MongoDbStop.class);
            morphia.map(MongoDbStopTime.class);
            morphia.map(MongoDbTrip.class);
            morphia.map(MongoDbRoute.class);
            morphia.map(MongoDbServiceCalendar.class);
            morphia.map(MongoDbServiceCalendarException.class);
        }

        ds = morphia.createDatastore(client, databaseName);
        ds.ensureIndexes();
        ds.ensureCaps();

        connections.put(databaseName, new DatabaseConnection(client, db, morphia, ds));
    }

    @Inject
    public MongoDb(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public MongoDatabase get(String databaseName) {
        if (databaseName == null) {
            return null;
        }
        if (!connections.containsKey(databaseName)) {
            connect(databaseName);
        }
        return connections.get(databaseName).db;
    }

    public Datastore getDs(String databaseName) {
        if (databaseName == null) {
            return null;
        }
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
