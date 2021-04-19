package models;

import entities.Stop;

import java.util.Map;

public interface StopsModel {
    void drop();

    Stop create(Map<String, String> data);
}
