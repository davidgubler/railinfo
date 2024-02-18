package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import configs.GtfsConfig;
import dev.morphia.InsertManyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Sort;
import dev.morphia.query.filters.Filters;
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

    private Query<MongoDbStopTime> query(GtfsConfig gtfs) {
        return gtfs.getDs().find(MongoDbStopTime.class);
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
        gtfs.getDs().save(serviceCalendarExceptions, new InsertManyOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
    }

    @Override
    public List<? extends StopTime> getByStops(GtfsConfig gtfs, Collection<? extends Stop> stops) {
        List<String> stopIds = stops.stream().map(Stop::getStopId).collect(Collectors.toList());
        List<MongoDbStopTime> stopTimes = query(gtfs).filter(Filters.in("stopId", stopIds)).iterator().toList();
        stopTimes.stream().forEach(st -> { st.setGtfs(gtfs); });
        return stopTimes;
    }

    @Override
    public List<MongoDbStopTime> getByTrip(GtfsConfig gtfs, Trip trip) {
        List<MongoDbStopTime> stopTimes = query(gtfs).filter(Filters.eq("tripId", trip.getTripId())).iterator(new FindOptions().sort(Sort.ascending("stopSequence"))).toList();
        stopTimes.stream().forEach(st -> { st.setGtfs(gtfs); });
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
        for (MongoDbStopTime stopTime : query(gtfs).filter(Filters.in("tripId", tripIds)).iterator().toList()) {
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
