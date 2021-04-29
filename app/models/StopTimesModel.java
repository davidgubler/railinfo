package models;

import entities.Stop;
import entities.StopTime;

import java.util.List;
import java.util.Map;

public interface StopTimesModel {
    void drop();

    StopTime create(Map<String, String> data);

    List<StopTime> getByStop(Stop stop);
}
