package models;

import entities.Trip;

import java.util.List;
import java.util.Map;

public interface TripsModel {

    void drop();

    Trip create(Map<String, String> data);

    List<Trip> create(List<Map<String, String>> dataBatch);

    Trip getByTripId(String id);
}
