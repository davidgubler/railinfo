package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import dev.morphia.InsertOptions;
import dev.morphia.query.UpdateOperations;
import entities.Stop;
import models.StopsModel;
import dev.morphia.query.Query;
import services.MongoDb;
import utils.Config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MongoDbStopsModel implements StopsModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    private Query<Stop> query() {
        return mongoDb.getDs(Config.TIMETABLE_DB).createQuery(Stop.class);
    }

    private UpdateOperations<Stop> ops() {
        return mongoDb.getDs(Config.TIMETABLE_DB).createUpdateOperations(Stop.class);
    }


    public void drop() {
        mongoDb.get(Config.TIMETABLE_DB).getCollection("stops").drop();
    }

    public Stop create(Map<String, String> data) {
        Stop stop = new Stop(data);
        mongoDb.getDs(Config.TIMETABLE_DB).save(stop);
        return stop;
    }

    @Override
    public List<Stop> create(List<Map<String, String>> dataBatch) {
        List<Stop> serviceCalendarExceptions = dataBatch.stream().map(data -> new Stop(data)).collect(Collectors.toList());
        mongoDb.getDs(Config.TIMETABLE_DB).save(serviceCalendarExceptions, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
        return serviceCalendarExceptions;
    }

    private ConcurrentHashMap<String, Stop> stopsCache = new ConcurrentHashMap<>();

    @Override
    public Stop getById(String stopId) {
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
    public void updateImportance(Set<Stop> stops, Integer importance) {
        Set<String> stopIds = stops.stream().map(Stop::getStopId).collect(Collectors.toSet());
        UpdateOperations<Stop> ops = ops().set("importance", importance);
        mongoDb.getDs(Config.TIMETABLE_DB).update(query().field("stopId").in(stopIds), ops);
    }

    private Map<String, Stop> stops = null;

    private Map<String, Stop> getStops() {
        if (this.stops == null) {
            List<Stop> stopsList = query().asList();

            Map<String, Stop> stops = new HashMap<>();
            for(Stop stop : stopsList) {
                Stop alreadyInMap = stops.get(stop.getParentStopId());
                if (alreadyInMap == null || alreadyInMap.getStopId().length() > stop.getStopId().length() ) {
                    stops.put(stop.getParentStopId(), stop);
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
}
