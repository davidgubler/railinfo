package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Sort;
import dev.morphia.query.experimental.filters.Filters;
import entities.StopTime;
import entities.Trip;
import models.TripsModel;
import dev.morphia.query.Query;
import services.MongoDb;

import java.util.Map;

public class MongoDbTripsModel implements TripsModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    @Override
    public void drop() {
        mongoDb.get().getCollection("trips").drop();
    }

    @Override
    public Trip create(Map<String, String> data) {
        Trip trip = new Trip(data);
        mongoDb.getDs().save(trip);
        return trip;
    }

    @Override
    public Trip getByTripId(String id) {
        Trip trip = mongoDb.getDs().find(Trip.class).filter(Filters.eq("tripId", id)).first();
        injector.injectMembers(trip);
        return trip;
    }
}
