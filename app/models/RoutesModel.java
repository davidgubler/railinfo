package models;

import configs.GtfsConfig;
import entities.Route;
import entities.mongodb.MongoDbRoute;

import java.util.List;
import java.util.Map;

public interface RoutesModel {

    void drop(GtfsConfig gtfs);

    MongoDbRoute create(GtfsConfig gtfs, Map<String, String> data);

    void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch);

    MongoDbRoute getByRouteId(GtfsConfig gtfs, String id);

    List<? extends Route> getByType(GtfsConfig gtfs, int min, int max);
}
