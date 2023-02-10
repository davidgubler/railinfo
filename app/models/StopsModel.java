package models;

import entities.Stop;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StopsModel {
    void drop();

    Stop create(Map<String, String> data);

    List<Stop> create(List<Map<String, String>> dataBatch);

    Stop create(String name, Double lat, Double lng);

    Stop get(String id);

    Stop getByStopId(String stopId);

    Set<Stop> getByName(String name);

    List<? extends Stop> getByPartialName(String name);

    void updateImportance(Set<Stop> stops, Integer importance);

    List<Stop> getAll();

    void update(Stop stop, String name, Double lat, Double lng);

    void delete(Stop stop);
}
