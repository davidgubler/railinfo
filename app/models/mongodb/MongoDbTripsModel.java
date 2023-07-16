package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import configs.GtfsConfig;
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

    private Query<Trip> query(GtfsConfig gtfs) {
        return gtfs.getDs().createQuery(Trip.class);
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        gtfs.getDatabase().getCollection("trips").drop();
    }

    @Override
    public Trip create(GtfsConfig gtfs, Map<String, String> data) {
        Trip trip = new Trip(data);
        gtfs.getDs().save(trip);
        return trip;
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        List<Trip> trips = dataBatch.stream().map(data -> new Trip(data)).collect(Collectors.toList());
        gtfs.getDs().save(trips, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
    }

    @Override
    public Trip getByTripId(GtfsConfig gtfs, String id) {
        Trip trip = query(gtfs).field("tripId").equal(id).get();
        injector.injectMembers(trip);
        trip.setGtfs(gtfs);
        return trip;
    }

    @Override
    public List<Trip> getByRoute(GtfsConfig gtfs, Route route) {
        List<Trip> trips = query(gtfs).field("routeId").equal(route.getRouteId()).asList();
        trips.stream().forEach(t -> { injector.injectMembers(t); t.setGtfs(gtfs); });
        return trips;
    }

    @Override
    public List<? extends Trip> getAll(GtfsConfig gtfs) {
        return query(gtfs).asList();
    }
}
