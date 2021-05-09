package models;

import entities.Stop;

import java.util.Map;
import java.util.Set;

public interface StopsModel {
    void drop();

    Stop create(Map<String, String> data);

    Set<Stop> getByName(String name);
}
