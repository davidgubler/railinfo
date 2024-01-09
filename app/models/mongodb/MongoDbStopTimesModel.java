package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import configs.GtfsConfig;
import dev.morphia.InsertOptions;
import entities.Stop;
import entities.StopTime;
import entities.Trip;
import entities.mongodb.MongoDbStopTime;
import models.StopTimesModel;
import dev.morphia.query.Query;
import services.MongoDb;

import java.util.*;
import java.util.stream.Collectors;

public class MongoDbStopTimesModel implements StopTimesModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    private Query<MongoDbStopTime> query(GtfsConfig gtfs) {
        return gtfs.getDs().createQuery(MongoDbStopTime.class);
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        gtfs.getDatabase().getCollection("stopTimes").drop();
    }

    @Override
    public MongoDbStopTime create(GtfsConfig gtfs, Map<String, String> data) {
        MongoDbStopTime stopTime = new MongoDbStopTime(data);
        gtfs.getDs().save(stopTime);
        return stopTime;
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        List<MongoDbStopTime> serviceCalendarExceptions = dataBatch.stream().map(data -> new MongoDbStopTime(data)).collect(Collectors.toList());
        gtfs.getDs().save(serviceCalendarExceptions, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
    }

    @Override
    public List<? extends StopTime> getByStops(GtfsConfig gtfs, Collection<? extends Stop> stops) {
        List<String> stopIds = stops.stream().map(Stop::getStopId).collect(Collectors.toList());
        List<MongoDbStopTime> stopTimes = query(gtfs).field("stopId").in(stopIds).asList();
        stopTimes.stream().forEach(st -> { injector.injectMembers(st); st.setGtfs(gtfs); });
        return stopTimes;
    }

    @Override
    public List<MongoDbStopTime> getByTrip(GtfsConfig gtfs, Trip trip) {
        List<MongoDbStopTime> stopTimes = query(gtfs).field("tripId").equal(trip.getTripId()).order("stopSequence").asList();
        stopTimes.stream().forEach(st -> { injector.injectMembers(st); st.setGtfs(gtfs); });
        return stopTimes;
    }

    @Override
    public Map<Trip, List<StopTime>> getByTrips(GtfsConfig gtfs, List<? extends Trip> trips) {
        Map<Trip, List<StopTime>> stopTimes = new HashMap<>();
        if (trips.isEmpty()) {
            return stopTimes;
        }

        Map<String, Trip> tripsMap = new HashMap<>();
        for (Trip trip : trips)  {
            tripsMap.put(trip.getTripId(), trip);
        }
        List<String> tripIds = trips.stream().map(Trip::getTripId).collect(Collectors.toList());
        for (MongoDbStopTime stopTime : query(gtfs).field("tripId").in(tripIds).asList()) {
            injector.injectMembers(stopTime);
            stopTime.setGtfs(gtfs);
            Trip trip = tripsMap.get(stopTime.getTripId());
            if (!stopTimes.containsKey(trip)) {
                stopTimes.put(trip, new LinkedList<>());
            }
            stopTimes.get(trip).add(stopTime);
        }

        for (Map.Entry<Trip, List<StopTime>> entry : stopTimes.entrySet()) {
            Collections.sort(entry.getValue());
        }

        return stopTimes;
    }
}
