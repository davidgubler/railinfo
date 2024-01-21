package models;

import configs.GtfsConfig;
import entities.Stop;
import entities.StopTime;
import entities.Trip;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface StopTimesModel {
    void drop(GtfsConfig gtfs);

    StopTime create(GtfsConfig gtfs, Map<String, String> data);

    void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch);

    List<? extends StopTime> getByStops(GtfsConfig gtfs, Collection<? extends Stop> stops);

    List<? extends StopTime> getByTrip(GtfsConfig gtfs, Trip trip);

    Map<Trip, List<StopTime>> getByTrips(GtfsConfig gtfs, List<? extends Trip> trips);
}
