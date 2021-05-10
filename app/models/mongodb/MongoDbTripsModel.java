package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
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

    private Query<Trip> query() {
        return mongoDb.getDs().createQuery(Trip.class);
    }

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
        Trip trip = query().field("tripId").equal(id).get();
        injector.injectMembers(trip);
        return trip;
    }
}
