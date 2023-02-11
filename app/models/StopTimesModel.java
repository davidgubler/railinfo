package models;

import entities.Stop;
import entities.StopTime;
import entities.Trip;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface StopTimesModel {
    void drop(String databaseName);

    StopTime create(String databaseName, Map<String, String> data);

    List<StopTime> create(String databaseName, List<Map<String, String>> dataBatch);

    List<StopTime> getByStops(String databaseName, Collection<? extends Stop> stops);

    List<StopTime> getByTrip(String databaseName, Trip trip);
}
