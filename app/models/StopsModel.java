package models;

import configs.GtfsConfig;
import entities.Stop;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface StopsModel {
    void drop(GtfsConfig gtfs);

    Stop create(GtfsConfig gtfs, Map<String, String> data);

    void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch);

    Stop create(GtfsConfig gtfs, String name, Double lat, Double lng);

    Stop create(GtfsConfig gtfs, String stopId, String name, Double lat, Double lng);

    Stop get(GtfsConfig gtfs, String id);

    Stop getByStopId(GtfsConfig gtfs, String stopId);

    Stop getByStopIdUncached(GtfsConfig gtfs, String stopId);

    Set<? extends Stop> getByName(GtfsConfig gtfs, String name);

    Stop getPrimaryByName(GtfsConfig gtfs, String name);

    List<? extends Stop> getByPartialName(GtfsConfig gtfs, String name);

    void updateImportance(GtfsConfig gtfs, Set<Stop> stops, Integer importance);

    List<Stop> getAll(GtfsConfig gtfs);

    Stream<Stop> getModified(GtfsConfig gtfs);

    void update(GtfsConfig gtfs, Stop stop, String name, Double lat, Double lng);

    void delete(GtfsConfig gtfs, Stop stop);
}
