package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import dev.morphia.InsertOptions;
import dev.morphia.query.UpdateOperations;
import dev.morphia.query.internal.MorphiaCursor;
import entities.Stop;
import entities.mongodb.MongoDbStop;
import models.StopsModel;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import services.MongoDb;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MongoDbStopsModel implements StopsModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    private Query<MongoDbStop> query(String databaseName) {
        return mongoDb.getDs(databaseName).createQuery(MongoDbStop.class);
    }

    private Query<MongoDbStop> query(String databaseName, MongoDbStop stop) {
        return mongoDb.getDs(databaseName).createQuery(MongoDbStop.class).field("_id").equal(stop.getObjectId());
    }

    private Query<MongoDbStop> queryId(String databaseName, ObjectId objectId) {
        return query(databaseName).field("_id").equal(objectId);
    }

    private UpdateOperations<MongoDbStop> ops(String databaseName) {
        return mongoDb.getDs(databaseName).createUpdateOperations(MongoDbStop.class);
    }

    public void drop(String databaseName) {
        mongoDb.get(databaseName).getCollection("stops").drop();
    }

    public MongoDbStop create(String databaseName, Map<String, String> data) {
        MongoDbStop stop = new MongoDbStop(data);
        injector.injectMembers(stop);
        stop.setDatabaseName(databaseName);
        mongoDb.getDs(databaseName).save(stop);
        stops.remove(databaseName);
        return stop;
    }

    @Override
    public void create(String databaseName, List<Map<String, String>> dataBatch) {
        List<MongoDbStop> stops = dataBatch.stream().map(data -> new MongoDbStop(data)).collect(Collectors.toList());
        mongoDb.getDs(databaseName).save(stops, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
        stops.remove(databaseName);
    }

    @Override
    public Stop create(String databaseName, String name, Double lat, Double lng) {
        Random rand = new Random();
        String stopId;
        do {
            stopId = "" + (Math.abs(rand.nextLong()) % 90000000000l + 10000000000l);
        } while (getByStopIdUncached(databaseName, stopId) != null);
        return create(databaseName, stopId, name, lat, lng);
    }

    @Override
    public Stop create(String databaseName, String stopId, String name, Double lat, Double lng) {
        MongoDbStop stop = new MongoDbStop(stopId, name, lat, lng);
        injector.injectMembers(stop);
        stop.setDatabaseName(databaseName);
        mongoDb.getDs(databaseName).save(stop);
        stops.remove(databaseName);
        return stop;
    }

    @Override
    public Stop get(String databaseName, String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (Exception e) {
            return null;
        }
        MongoDbStop stop = query(databaseName).field("_id").equal(objectId).first();
        if (stop != null) {
            injector.injectMembers(stop);
            stop.setDatabaseName(databaseName);
        }
        return stop;
    }

    @Override
    public MongoDbStop getByStopId(String databaseName, String stopId) {
        if (stopId == null) {
            return null;
        }
        return getStops(databaseName).get(stopId);
    }

    @Override
    public Stop getByStopIdUncached(String databaseName, String stopId) {
        if (stopId == null) {
            return null;
        }
        MongoDbStop stop = query(databaseName).field("stopId").equal(stopId).first();
        if (stop == null) {
            return null;
        }
        injector.injectMembers(stop);
        stop.setDatabaseName(databaseName);
        return stop;
    }

    @Override
    public Set<? extends Stop> getByName(String databaseName, String name) {
        Set<MongoDbStop> stops = new HashSet<>();
        stops.addAll(query(databaseName).field("name").equal(name).asList());
        stops.stream().forEach(s -> { injector.injectMembers(s); s.setDatabaseName(databaseName);});
        return stops;
    }

    @Override
    public Stop getPrimaryByName(String databaseName, String name) {
        Set<? extends Stop> stops = getByName(databaseName, name);
        if (stops.size() <= 1) {
            return stops.stream().findFirst().orElse(null);
        }
        Map<String, Stop> stopsById = new HashMap<>();
        for (Stop stop : stops) {
            stopsById.put(stop.getStopId(), stop);
        }
        Stop stop = stops.stream().findAny().get();
        // we try to find the parent twice, just in case somebody has double-nested stops
        Stop parent = stopsById.get(stop.getParentId());
        if (parent != null) {
            stop = parent;
        }
        parent = stopsById.get(stop.getParentId());
        if (parent != null) {
            stop = parent;
        }
        return stop;
    }

    @Override
    public List<? extends MongoDbStop> getByPartialName(String databaseName, String name) {
        List<MongoDbStop> stops = query(databaseName).search(name).find().toList();
        stops.stream().forEach(s -> { injector.injectMembers(s); s.setDatabaseName(databaseName);});
        return stops;
    }

    @Override
    public void updateImportance(String databaseName, Set<Stop> stops, Integer importance) {
        Set<String> stopIds = stops.stream().map(Stop::getStopId).collect(Collectors.toSet());
        UpdateOperations<MongoDbStop> ops = ops(databaseName).set("importance", importance);
        mongoDb.getDs(databaseName).update(query(databaseName).field("stopId").in(stopIds), ops);
        stops.remove(databaseName);
    }

    private ConcurrentHashMap<String, Map<String, MongoDbStop>> stops = new ConcurrentHashMap<>();

    private Map<String, MongoDbStop> getStops(String databaseName) {
        if (!stops.containsKey(databaseName)) {
            Map<String, MongoDbStop> stops = new HashMap<>();
            MorphiaCursor<MongoDbStop> stopsCursor = query(databaseName).find();
            while (stopsCursor.hasNext()) {
                MongoDbStop stop = stopsCursor.next();
                injector.injectMembers(stop);
                stop.setDatabaseName(databaseName);
                
                stops.put(stop.getStopId(), stop);

                // We have some applications (edges in particular) that use the baseId. It is important to make
                // stops available via baseId as well.
                String baseId = stop.getBaseId();
                if (!stops.containsKey(baseId) || stop.getParentId() == null) {
                    // we try to have the baseId point to the parent
                    stops.put(baseId, stop);
                }
            }
            stopsCursor.close();
            this.stops.put(databaseName, Collections.unmodifiableMap(stops));
        }
        return stops.get(databaseName);
    }

    @Override
    public List<Stop> getAll(String databaseName) {
        return getStops(databaseName).entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toList());
    }

    @Override
    public Stream<Stop> getModified(String databaseName) {
        return getAll(databaseName).stream().filter(Stop::isModified);
    }

    @Override
    public void update(String databaseName, Stop stop, String name, Double lat, Double lng) {
        MongoDbStop mongoDbStop = (MongoDbStop)stop;
        mongoDbStop.setName(name);
        mongoDbStop.setLat(lat);
        mongoDbStop.setLng(lng);
        mongoDbStop.setModified(true);
        UpdateOperations<MongoDbStop> ops = ops(databaseName).set("name", name).set("lat", lat).set("lng", lng).set("modified", Boolean.TRUE);
        mongoDb.getDs(databaseName).update(query(databaseName, mongoDbStop), ops);
        stops.remove(databaseName);
    }

    @Override
    public void delete(String databaseName, Stop stop) {
        MongoDbStop mongoDbStop = (MongoDbStop)stop;
        mongoDb.getDs(databaseName).delete(query(databaseName, mongoDbStop));
        stops.remove(databaseName);
    }
}
