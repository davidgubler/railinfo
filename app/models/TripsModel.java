package models;

import entities.Route;
import entities.Trip;
import entities.realized.RealizedTrip;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TripsModel {

    void drop();

    Trip create(Map<String, String> data);

    List<Trip> create(List<Map<String, String>> dataBatch);

    Trip getByTripId(String id);

    List<Trip> getByRoute(Route route);

    RealizedTrip getRealizedTrip(String id, LocalDate date);
}
