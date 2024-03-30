package services;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import configs.CH;
import configs.GtfsConfig;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;
import entities.mongodb.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MongoDb {
    private static final int TIMEOUT_CONNECT = 15 * 1000; // 15 seconds

    private final Map<String, Datastore> connections = new ConcurrentHashMap<>(1);

    private MongoClient client;

    private final ActorSystem actorSystem;

    private MongoClient getClient() {
        if (client == null) {
            Boolean tls = false;
            String hostname = "localhost";
            String username = null;
            String password = null;
            String mongoUrl;
            if (username != null && password != null) {
                mongoUrl = "mongodb://" + username + ":" + password + "@" + hostname + ":27017/?tls=" + tls.toString().toLowerCase() + "&connecttimeoutms=" + TIMEOUT_CONNECT;
            } else {
                mongoUrl = "mongodb://" + hostname + ":27017/?tls=" + tls.toString().toLowerCase() + "&connecttimeoutms=" + TIMEOUT_CONNECT;
            }
            client = MongoClients.create(mongoUrl);
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
        String prefix = "railinfo-" + countryCode + "-";
        int length = prefix.length() + 10; // 10 for the ISO date
        while (i.hasNext()) {
            String dbName = i.next();
            if (dbName.startsWith(prefix) && dbName.length() == length) {
                databases.add(dbName);
            }
        }
        Collections.sort(databases);
        Collections.reverse(databases);
        return databases;
    }

    private void connect(String databaseName) {
        MongoClient client = getClient();
        Datastore ds;

        MapperOptions mapperOptions = MapperOptions.builder().storeEmpties(false).storeNulls(false).build();
        ds = Morphia.createDatastore(client, databaseName, mapperOptions);

        if (!databaseName.contains("-")) {
            // it is the global database
            ds.getMapper().map(MongoDbUser.class);
        } else {
            // it is a timetable database
            ds.getMapper().map(MongoDbEdge.class);
            ds.getMapper().map(MongoDbStop.class);
            ds.getMapper().map(MongoDbStopTime.class);
            ds.getMapper().map(MongoDbTrip.class);
            ds.getMapper().map(MongoDbRoute.class);
            ds.getMapper().map(MongoDbServiceCalendar.class);
            ds.getMapper().map(MongoDbServiceCalendarException.class);
        }

        ds.ensureIndexes();
        ds.ensureCaps();

        connections.put(databaseName, ds);
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
        return connections.get(databaseName).getDatabase();
    }

    public Datastore getDs(String databaseName) {
        if (databaseName == null) {
            return null;
        }
        if (!connections.containsKey(databaseName)) {
            connect(databaseName);
        }
        return connections.get(databaseName);
    }
}
