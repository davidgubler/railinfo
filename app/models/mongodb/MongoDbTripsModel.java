package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import configs.GtfsConfig;
import dev.morphia.InsertManyOptions;
import dev.morphia.query.filters.Filters;
import entities.Route;
import entities.Trip;
import entities.mongodb.MongoDbTrip;
import models.TripsModel;
import dev.morphia.query.Query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoDbTripsModel implements TripsModel {

    @Inject
    private Injector injector;

    private Query<MongoDbTrip> query(GtfsConfig gtfs) {
        return gtfs.getDs().find(MongoDbTrip.class);
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        gtfs.getDatabase().getCollection("trips").drop();
    }

    @Override
    public Trip create(GtfsConfig gtfs, Map<String, String> data) {
        Trip trip = new MongoDbTrip(data);
        gtfs.getDs().save(trip);
        return trip;
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        List<Trip> trips = dataBatch.stream().map(data -> new MongoDbTrip(data)).collect(Collectors.toList());
        gtfs.getDs().save(trips, new InsertManyOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
    }

    @Override
    public Trip getByTripId(GtfsConfig gtfs, String id) {
        MongoDbTrip trip = query(gtfs).filter(Filters.eq("tripId", id)).first();
        injector.injectMembers(trip);
        trip.setGtfs(gtfs);
        return trip;
    }

    @Override
    public List<? extends Trip> getByRoute(GtfsConfig gtfs, Route route) {
        List<MongoDbTrip> trips = query(gtfs).filter(Filters.eq("routeId", route.getRouteId())).iterator().toList();
        trips.stream().forEach(t -> { injector.injectMembers(t); t.setGtfs(gtfs); });
        return trips;
    }

    @Override
    public List<? extends Trip> getAll(GtfsConfig gtfs) {
        return query(gtfs).iterator().toList();
    }
}
