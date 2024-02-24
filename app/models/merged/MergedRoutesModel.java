package models.merged;

import configs.GtfsConfig;
import entities.Route;
import entities.mongodb.MongoDbRoute;
import models.RoutesModel;

import java.util.*;

public class MergedRoutesModel implements RoutesModel {

    private final List<GtfsConfig> subConfigs;

    public MergedRoutesModel(List<GtfsConfig> subConfigs) {
        this.subConfigs = subConfigs;
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        throw new IllegalStateException();
    }

    @Override
    public MongoDbRoute create(GtfsConfig gtfs, Map<String, String> data) {
        throw new IllegalStateException();
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        throw new IllegalStateException();
    }

    @Override
    public MongoDbRoute getByRouteId(GtfsConfig gtfs, String id) {
        for (GtfsConfig subCfg : subConfigs) {
            MongoDbRoute route = subCfg.getRoutesModel().getByRouteId(subCfg, id);
            if (route != null) {
                return route;
            }
        }
        return null;
    }

    @Override
    public List<? extends Route> getByType(GtfsConfig gtfs, int min, int max) {
        throw new IllegalStateException();
    }

    public List<? extends Route> getRailRoutes() {
        Set<Route> routeSet = new HashSet<>();
        for (GtfsConfig subCfg : subConfigs) {
            routeSet.addAll(subCfg.getRailRoutes());
        }
        List<Route> routes = new LinkedList<>(routeSet);
        return routes;
    }
}
