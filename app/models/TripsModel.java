package models;

import entities.Route;
import entities.Trip;
import entities.realized.RealizedTrip;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TripsModel {

    void drop(String databaseName);

    Trip create(String databaseName, Map<String, String> data);

    void create(String databaseName, List<Map<String, String>> dataBatch);

    Trip getByTripId(String databaseName, String id);

    List<Trip> getByRoute(String databaseName, Route route);

    RealizedTrip getRealizedTrip(String databaseName, String id, LocalDate date);

    List<? extends Trip> getAll(String databaseName);
}
