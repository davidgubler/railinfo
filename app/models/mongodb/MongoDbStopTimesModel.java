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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoDbStopTimesModel implements StopTimesModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    private Query<StopTime> query() {
        return mongoDb.getDs().createQuery(StopTime.class);
    }

    @Override
    public void drop() {
        mongoDb.get().getCollection("stopTimes").drop();
    }

    @Override
    public StopTime create(Map<String, String> data) {
        StopTime stopTime = new StopTime(data);
        mongoDb.getDs().save(stopTime);
        return stopTime;
    }

    @Override
    public List<StopTime> create(List<Map<String, String>> dataBatch) {
        List<StopTime> serviceCalendarExceptions = dataBatch.stream().map(data -> new StopTime(data)).collect(Collectors.toList());
        mongoDb.getDs().save(serviceCalendarExceptions, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
        return serviceCalendarExceptions;
    }

    @Override
    public List<StopTime> getByStops(Collection<Stop> stops) {
        List<String> stopIds = stops.stream().map(Stop::getStopId).collect(Collectors.toList());
        return query().field("stopId").in(stopIds).asList();
    }

    @Override
    public List<StopTime> getByTrip(Trip trip) {
        return query().field("tripId").equal(trip.getTripId()).order("stopSequence").asList();
    }
}
