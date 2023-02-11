package models;

import entities.Stop;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StopsModel {
    void drop(String databaseName);

    Stop create(String databaseName, Map<String, String> data);

    List<Stop> create(String databaseName, List<Map<String, String>> dataBatch);

    Stop create(String databaseName, String name, Double lat, Double lng);

    Stop get(String databaseName, String id);

    Stop getByStopId(String databaseName, String stopId);

    Set<? extends Stop> getByName(String databaseName, String name);

    Stop getPrimaryByName(String databaseName, String name);

    List<? extends Stop> getByPartialName(String databaseName, String name);

    void updateImportance(String databaseName, Set<Stop> stops, Integer importance);

    List<Stop> getAll(String databaseName);

    void update(String databaseName, Stop stop, String name, Double lat, Double lng);

    void delete(String databaseName, Stop stop);
}
