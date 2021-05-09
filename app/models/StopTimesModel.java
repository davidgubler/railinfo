package models;

import entities.Stop;
import entities.StopTime;
import entities.Trip;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface StopTimesModel {
    void drop();

    StopTime create(Map<String, String> data);

    List<StopTime> getByStops(Collection<Stop> stops);

    List<StopTime> getByTrip(Trip trip);
}
