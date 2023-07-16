package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import configs.GtfsConfig;
import dev.morphia.InsertOptions;
import dev.morphia.query.UpdateOperations;
import dev.morphia.query.internal.MorphiaCursor;
import entities.Stop;
import entities.mongodb.MongoDbStop;
import models.StopsModel;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MongoDbStopsModel implements StopsModel {

    @Inject
    private Injector injector;

    private Query<MongoDbStop> query(GtfsConfig gtfs) {
        return gtfs.getDs().createQuery(MongoDbStop.class);
    }

    private Query<MongoDbStop> query(GtfsConfig gtfs, MongoDbStop stop) {
        return gtfs.getDs().createQuery(MongoDbStop.class).field("_id").equal(stop.getObjectId());
    }

    private Query<MongoDbStop> queryId(GtfsConfig gtfs, ObjectId objectId) {
        return query(gtfs).field("_id").equal(objectId);
    }

    private UpdateOperations<MongoDbStop> ops(GtfsConfig gtfs) {
        return gtfs.getDs().createUpdateOperations(MongoDbStop.class);
    }

    public void drop(GtfsConfig gtfs) {
        gtfs.getDatabase().getCollection("stops").drop();
    }

    public MongoDbStop create(GtfsConfig gtfs, Map<String, String> data) {
        MongoDbStop stop = new MongoDbStop(data);
        injector.injectMembers(stop);
        stop.setGtfs(gtfs);
        gtfs.getDs().save(stop);
        stops.remove(gtfs);
        return stop;
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        List<MongoDbStop> stops = dataBatch.stream().map(data -> new MongoDbStop(data)).collect(Collectors.toList());
        gtfs.getDs().save(stops, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
        stops.remove(gtfs);
    }

    @Override
    public Stop create(GtfsConfig gtfs, String name, Double lat, Double lng) {
        Random rand = new Random();
        String stopId;
        do {
            stopId = "" + (Math.abs(rand.nextLong()) % 90000000000l + 10000000000l);
        } while (getByStopIdUncached(gtfs, stopId) != null);
        return create(gtfs, stopId, name, lat, lng);
    }

    @Override
    public Stop create(GtfsConfig gtfs, String stopId, String name, Double lat, Double lng) {
        MongoDbStop stop = new MongoDbStop(stopId, name, lat, lng);
        injector.injectMembers(stop);
        stop.setGtfs(gtfs);
        gtfs.getDs().save(stop);
        stops.remove(gtfs);
        return stop;
    }

    @Override
    public Stop get(GtfsConfig gtfs, String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (Exception e) {
            return null;
        }
        MongoDbStop stop = query(gtfs).field("_id").equal(objectId).first();
        if (stop != null) {
            injector.injectMembers(stop);
            stop.setGtfs(gtfs);
        }
        return stop;
    }

    @Override
    public MongoDbStop getByStopId(GtfsConfig gtfs, String stopId) {
        if (stopId == null) {
            return null;
        }
        return getStops(gtfs).get(stopId);
    }

    @Override
    public Stop getByStopIdUncached(GtfsConfig gtfs, String stopId) {
        if (stopId == null) {
            return null;
        }
        MongoDbStop stop = query(gtfs).field("stopId").equal(stopId).first();
        if (stop == null) {
            return null;
        }
        injector.injectMembers(stop);
        stop.setGtfs(gtfs);
        return stop;
    }

    @Override
    public Set<? extends Stop> getByName(GtfsConfig gtfs, String name) {
        Set<MongoDbStop> stops = new HashSet<>();
        stops.addAll(query(gtfs).field("name").equal(name).asList());
        stops.stream().forEach(s -> { injector.injectMembers(s); s.setGtfs(gtfs);});
        return stops;
    }

    @Override
    public Stop getPrimaryByName(GtfsConfig gtfs, String name) {
        Set<? extends Stop> stops = getByName(gtfs, name);
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
    public List<? extends MongoDbStop> getByPartialName(GtfsConfig gtfs, String name) {
        List<MongoDbStop> stops = query(gtfs).search(name).find().toList();
        stops.stream().forEach(s -> { injector.injectMembers(s); s.setGtfs(gtfs);});
        return stops;
    }

    @Override
    public void updateImportance(GtfsConfig gtfs, Set<Stop> stops, Integer importance) {
        Set<String> stopIds = stops.stream().map(Stop::getStopId).collect(Collectors.toSet());
        UpdateOperations<MongoDbStop> ops = ops(gtfs).set("importance", importance);
        gtfs.getDs().update(query(gtfs).field("stopId").in(stopIds), ops);
        stops.remove(gtfs);
    }

    private ConcurrentHashMap<GtfsConfig, Map<String, MongoDbStop>> stops = new ConcurrentHashMap<>();

    private Map<String, MongoDbStop> getStops(GtfsConfig gtfs) {
        if (!stops.containsKey(gtfs)) {
            Map<String, MongoDbStop> stops = new HashMap<>();
            MorphiaCursor<MongoDbStop> stopsCursor = query(gtfs).find();
            while (stopsCursor.hasNext()) {
                MongoDbStop stop = stopsCursor.next();
                injector.injectMembers(stop);
                stop.setGtfs(gtfs);
                
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
            this.stops.put(gtfs, Collections.unmodifiableMap(stops));
        }
        return stops.get(gtfs);
    }

    @Override
    public List<Stop> getAll(GtfsConfig gtfs) {
        return getStops(gtfs).entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toList());
    }

    @Override
    public Stream<Stop> getModified(GtfsConfig gtfs) {
        return getAll(gtfs).stream().filter(Stop::isModified);
    }

    @Override
    public void update(GtfsConfig gtfs, Stop stop, String name, Double lat, Double lng) {
        MongoDbStop mongoDbStop = (MongoDbStop)stop;
        mongoDbStop.setName(name);
        mongoDbStop.setLat(lat);
        mongoDbStop.setLng(lng);
        mongoDbStop.setModified(true);
        UpdateOperations<MongoDbStop> ops = ops(gtfs).set("name", name).set("lat", lat).set("lng", lng).set("modified", Boolean.TRUE);
        gtfs.getDs().update(query(gtfs, mongoDbStop), ops);
        stops.remove(gtfs);
    }

    @Override
    public void delete(GtfsConfig gtfs, Stop stop) {
        MongoDbStop mongoDbStop = (MongoDbStop)stop;
        gtfs.getDs().delete(query(gtfs, mongoDbStop));
        stops.remove(gtfs);
    }
}
