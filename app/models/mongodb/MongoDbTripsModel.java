package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import dev.morphia.InsertOptions;
import entities.Route;
import entities.Trip;
import entities.realized.RealizedTrip;
import models.TripsModel;
import dev.morphia.query.Query;
import services.MongoDb;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoDbTripsModel implements TripsModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    private Query<Trip> query(String databaseName) {
        return mongoDb.getDs(databaseName).createQuery(Trip.class);
    }

    @Override
    public void drop(String databaseName) {
        mongoDb.get(databaseName).getCollection("trips").drop();
    }

    @Override
    public Trip create(String databaseName, Map<String, String> data) {
        Trip trip = new Trip(data);
        mongoDb.getDs(databaseName).save(trip);
        return trip;
    }

    @Override
    public void create(String databaseName, List<Map<String, String>> dataBatch) {
        List<Trip> trips = dataBatch.stream().map(data -> new Trip(data)).collect(Collectors.toList());
        mongoDb.getDs(databaseName).save(trips, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
    }

    @Override
    public Trip getByTripId(String databaseName, String id) {
        Trip trip = query(databaseName).field("tripId").equal(id).get();
        injector.injectMembers(trip);
        trip.setDatabaseName(databaseName);
        return trip;
    }

    @Override
    public List<Trip> getByRoute(String databaseName, Route route) {
        List<Trip> trips = query(databaseName).field("routeId").equal(route.getRouteId()).asList();
        trips.stream().forEach(t -> { injector.injectMembers(t); t.setDatabaseName(databaseName); });
        return trips;
    }

    @Override
    public RealizedTrip getRealizedTrip(String databaseName, String id, LocalDate date) {
        Trip trip = getByTripId(databaseName, id);
        if (trip == null || !trip.isActive(date)) {
            return null;
        }
        RealizedTrip realizedTrip = new RealizedTrip(trip, date);
        injector.injectMembers(realizedTrip);
        realizedTrip.setDatabaseName(databaseName);
        return realizedTrip;
    }

    @Override
    public List<? extends Trip> getAll(String databaseName) {
        return query(databaseName).asList();
    }
}
