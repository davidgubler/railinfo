package models;

import configs.GtfsConfig;
import entities.Route;
import entities.Trip;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface TripsModel {

    void drop(GtfsConfig gtfs);

    Trip create(GtfsConfig gtfs, Map<String, String> data);

    void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch);

    Trip getByTripId(GtfsConfig gtfs, String id);

    Stream<? extends Trip> getByRoute(Route route);

    List<? extends Trip> getAll(GtfsConfig gtfs);
}
