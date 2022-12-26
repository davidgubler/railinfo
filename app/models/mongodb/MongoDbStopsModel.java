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
import java.util.regex.Pattern;
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

    @Override
    public Stop getById(String stopId) {
        Stop stop = getStops().get(stopId);
        if (stop == null && !stopId.endsWith("P")) {
            stop = getStops().get(stopId + "P");
        }
        if (stop == null) {
            Pattern regexp = Pattern.compile("^" + stopId + ":");
            stop = query().filter("stopId", regexp).get();
        }
        if (stop == null) {
            System.out.println("ERROR: stopId " + stopId + " not found");
        }
        injector.injectMembers(stop);
        return stop;
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
                stops.put(stop.getStopId(), stop);
            }
            this.stops = Collections.unmodifiableMap(stops);
        }
        return this.stops;
    }
}
