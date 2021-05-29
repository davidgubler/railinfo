package models;

import entities.Route;

import java.util.List;
import java.util.Map;

public interface RoutesModel {

    void drop();

    Route create(Map<String, String> data);

    List<Route> create(List<Map<String, String>> dataBatch);

    Route getByRouteId(String id);
}
