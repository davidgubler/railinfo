package models;

import entities.Stop;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StopsModel {
    void drop();

    Stop create(Map<String, String> data);

    List<Stop> create(List<Map<String, String>> dataBatch);

    Stop getById(String stopId);

    Set<Stop> getByName(String name);

    void updateImportance(Set<Stop> stops, Integer importance);
}
