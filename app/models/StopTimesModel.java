package models;

import entities.StopTime;

import java.util.Map;

public interface StopTimesModel {
    void drop();
    StopTime create(Map<String, String> data);
}
