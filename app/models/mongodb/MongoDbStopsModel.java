package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import dev.morphia.InsertOptions;
import dev.morphia.query.UpdateOperations;
import entities.Stop;
import entities.mongodb.MongoDbStop;
import models.StopsModel;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import services.MongoDb;
import utils.Config;

import java.util.*;
import java.util.stream.Collectors;

public class MongoDbStopsModel implements StopsModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    private Query<MongoDbStop> query() {
        return mongoDb.getDs(Config.TIMETABLE_DB).createQuery(MongoDbStop.class);
    }

    private Query<MongoDbStop> query(MongoDbStop stop) {
        return mongoDb.getDs(Config.TIMETABLE_DB).createQuery(MongoDbStop.class).field("_id").equal(stop.getObjectId());
    }

    private Query<MongoDbStop> queryId(ObjectId objectId) {
        return query().field("_id").equal(objectId);
    }

    private UpdateOperations<MongoDbStop> ops() {
        return mongoDb.getDs(Config.TIMETABLE_DB).createUpdateOperations(MongoDbStop.class);
    }


    public void drop() {
        mongoDb.get(Config.TIMETABLE_DB).getCollection("stops").drop();
    }

    public MongoDbStop create(Map<String, String> data) {
        MongoDbStop stop = new MongoDbStop(data);
        mongoDb.getDs(Config.TIMETABLE_DB).save(stop);
        stops = null;
        return stop;
    }

    @Override
    public List<Stop> create(List<Map<String, String>> dataBatch) {
        List<Stop> serviceCalendarExceptions = dataBatch.stream().map(data -> new MongoDbStop(data)).collect(Collectors.toList());
        mongoDb.getDs(Config.TIMETABLE_DB).save(serviceCalendarExceptions, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
        stops = null;
        return serviceCalendarExceptions;
    }

    @Override
    public Stop create(String name, Double lat, Double lng) {
        Random rand = new Random();

        String stopId;
        do {
            stopId = "" + (Math.abs(rand.nextLong()) % 90000000000l + 10000000000l);
        } while (getByStopId(stopId) != null);

        MongoDbStop stop = new MongoDbStop(stopId, name, lat, lng);
        mongoDb.getDs(Config.TIMETABLE_DB).save(stop);
        stops = null;
        return stop;
    }

    @Override
    public Stop get(String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (Exception e) {
            return null;
        }
        Stop stop = query().field("_id").equal(objectId).first();
        if (stop != null) {
            injector.injectMembers(stop);
        }
        return stop;
    }

    @Override
    public MongoDbStop getByStopId(String stopId) {
        if (stopId == null) {
            return null;
        }
        stopId = stopId.split(":")[0];
        if (stopId.endsWith("P")) {
            stopId = stopId.substring(0, stopId.length() - 1);
        }
        return getStops().get(stopId);
    }

    @Override
    public Set<Stop> getByName(String name) {
        Set<Stop> stops = new HashSet<>();
        stops.addAll(query().field("name").equal(name).asList());
        stops.stream().forEach(s -> injector.injectMembers(s));
        return stops;
    }

    @Override
    public Stop getPrimaryByName(String name) {
        Set<Stop> stops = getByName(name);
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
    public List<? extends MongoDbStop> getByPartialName(String name) {
        List<MongoDbStop> stops = query().search(name).find().toList();
        stops.stream().forEach(s -> injector.injectMembers(s));
        return stops;
    }

    @Override
    public void updateImportance(Set<Stop> stops, Integer importance) {
        Set<String> stopIds = stops.stream().map(Stop::getStopId).collect(Collectors.toSet());
        UpdateOperations<MongoDbStop> ops = ops().set("importance", importance);
        mongoDb.getDs(Config.TIMETABLE_DB).update(query().field("stopId").in(stopIds), ops);
        stops = null;
    }

    private Map<String, MongoDbStop> stops = null;

    private Map<String, MongoDbStop> getStops() {
        if (this.stops == null) {
            List<MongoDbStop> stopsList = query().asList();

            Map<String, MongoDbStop> stops = new HashMap<>();
            for(MongoDbStop stop : stopsList) {
                MongoDbStop alreadyInMap = stops.get(stop.getBaseId());
                if (alreadyInMap == null || alreadyInMap.getStopId().length() > stop.getStopId().length() ) {
                    injector.injectMembers(stop);
                    stops.put(stop.getBaseId(), stop);
                }
            }
            System.out.println("found " + stopsList.size() + " stops, combined into " + stops.keySet().size());
            this.stops = Collections.unmodifiableMap(stops);
        }
        return this.stops;
    }

    @Override
    public List<Stop> getAll() {
        return getStops().entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toList());
    }

    @Override
    public void update(Stop stop, String name, Double lat, Double lng) {
        MongoDbStop mongoDbStop = (MongoDbStop)stop;
        mongoDbStop.setName(name);
        mongoDbStop.setLat(lat);
        mongoDbStop.setLng(lng);
        mongoDbStop.setModified(true);
        UpdateOperations<MongoDbStop> ops = ops().set("name", name).set("lat", lat).set("lng", lng).set("modified", Boolean.TRUE);
        mongoDb.getDs(Config.TIMETABLE_DB).update(query(mongoDbStop), ops);
        stops = null;
    }

    @Override
    public void delete(Stop stop) {
        MongoDbStop mongoDbStop = (MongoDbStop)stop;
        mongoDb.getDs(Config.TIMETABLE_DB).delete(query(mongoDbStop));
        stops = null;
    }
}
