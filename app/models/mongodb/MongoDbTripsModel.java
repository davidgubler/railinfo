package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import dev.morphia.InsertOptions;
import entities.Route;
import entities.Trip;
import models.TripsModel;
import dev.morphia.query.Query;
import services.MongoDb;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<Trip> create(List<Map<String, String>> dataBatch) {
        List<Trip> trips = dataBatch.stream().map(data -> new Trip(data)).collect(Collectors.toList());
        mongoDb.getDs().save(trips, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
        return trips;
    }

    @Override
    public Trip getByTripId(String id) {
        Trip trip = query().field("tripId").equal(id).get();
        injector.injectMembers(trip);
        return trip;
    }

    @Override
    public List<Trip> getByRoute(Route route) {
        List<Trip> trips = query().field("routeId").equal(route.getRouteId()).asList();
        trips.stream().forEach(t -> injector.injectMembers(t));
        return trips;
    }
}
