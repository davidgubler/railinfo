package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Sort;
import dev.morphia.query.experimental.filters.Filters;
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
    public List<StopTime> getByStops(Collection<Stop> stops) {
        List<String> stopIds = stops.stream().map(Stop::getStopId).collect(Collectors.toList());
        return mongoDb.getDs().find(StopTime.class).filter(Filters.in("stopId", stopIds)).iterator().toList();
    }

    @Override
    public List<StopTime> getByTrip(Trip trip) {
        return mongoDb.getDs().find(StopTime.class).filter(Filters.eq("tripId", trip.getTripId())).iterator(new FindOptions().sort(Sort.ascending("stopSequence"))).toList();
    }
}
