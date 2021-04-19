package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.Trip;
import models.TripsModel;
import services.MongoDb;

import java.util.Map;

public class MongoDbTripsModel implements TripsModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    public void drop() {
        mongoDb.get().getCollection("trips").drop();
    }

    public Trip create(Map<String, String> data) {
        Trip trip = new Trip(data);
        mongoDb.getDs().save(trip);
        return trip;
    }
}
