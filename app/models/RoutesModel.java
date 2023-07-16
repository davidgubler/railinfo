package models;

import configs.GtfsConfig;
import entities.Route;

import java.util.List;
import java.util.Map;

public interface RoutesModel {

    void drop(GtfsConfig gtfs);

    Route create(GtfsConfig gtfs, Map<String, String> data);

    void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch);

    Route getByRouteId(GtfsConfig gtfs, String id);

    List<Route> getByType(GtfsConfig gtfs, int from, int to);
}
