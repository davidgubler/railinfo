package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import dev.morphia.InsertOptions;
import entities.Stop;
import entities.StopTime;
import entities.Trip;
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

    private Query<StopTime> query(String databaseName) {
        return mongoDb.getDs(databaseName).createQuery(StopTime.class);
    }

    @Override
    public void drop(String databaseName) {
        mongoDb.get(databaseName).getCollection("stopTimes").drop();
    }

    @Override
    public StopTime create(String databaseName, Map<String, String> data) {
        StopTime stopTime = new StopTime(data);
        mongoDb.getDs(databaseName).save(stopTime);
        return stopTime;
    }

    @Override
    public List<StopTime> create(String databaseName, List<Map<String, String>> dataBatch) {
        List<StopTime> serviceCalendarExceptions = dataBatch.stream().map(data -> new StopTime(data)).collect(Collectors.toList());
        mongoDb.getDs(databaseName).save(serviceCalendarExceptions, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
        return serviceCalendarExceptions;
    }

    @Override
    public List<StopTime> getByStops(String databaseName, Collection<? extends Stop> stops) {
        List<String> stopIds = stops.stream().map(Stop::getStopId).collect(Collectors.toList());
        List<StopTime> stopTimes = query(databaseName).field("stopId").in(stopIds).asList();
        stopTimes.stream().forEach(st -> { injector.injectMembers(st); st.setDatabaseName(databaseName); });
        return stopTimes;
    }

    @Override
    public List<StopTime> getByTrip(String databaseName, Trip trip) {
        List<StopTime> stopTimes = query(databaseName).field("tripId").equal(trip.getTripId()).order("stopSequence").asList();
        stopTimes.stream().forEach(st -> { injector.injectMembers(st); st.setDatabaseName(databaseName); });
        return stopTimes;
    }
}
