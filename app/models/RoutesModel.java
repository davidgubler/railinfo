package models;

import entities.Route;

import java.util.List;
import java.util.Map;

public interface RoutesModel {

    void drop(String databaseName);

    Route create(String databaseName, Map<String, String> data);

    void create(String databaseName, List<Map<String, String>> dataBatch);

    Route getByRouteId(String databaseName, String id);

    List<Route> getByType(String databaseName, int from, int to);
}
