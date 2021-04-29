package models;

import entities.Trip;

import java.util.Map;

public interface TripsModel {

    void drop();

    Trip create(Map<String, String> data);

    Trip getByTripId(String id);
}
