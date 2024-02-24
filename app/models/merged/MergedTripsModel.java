package models.merged;

import configs.GtfsConfig;
import entities.Route;
import entities.Trip;
import models.TripsModel;

import java.util.*;

public class MergedTripsModel implements TripsModel {

    private final List<GtfsConfig> subConfigs;

    public MergedTripsModel(List<GtfsConfig> subConfigs) {
        this.subConfigs = subConfigs;
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        throw new IllegalStateException();
    }

    @Override
    public Trip create(GtfsConfig gtfs, Map<String, String> data) {
        throw new IllegalStateException();
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        throw new IllegalStateException();
    }

    @Override
    public Trip getByTripId(GtfsConfig gtfs, String id) {
        for (GtfsConfig subCfg : subConfigs) {
            Trip trip = subCfg.getTripsModel().getByTripId(subCfg, id);
            if (trip != null) {
                return trip;
            }
        }
        return null;
    }

    @Override
    public List<? extends Trip> getByRoute(Route route) {
        return route.getSourceGtfs().getTripsModel().getByRoute(route);
    }

    @Override
    public List<? extends Trip> getAll(GtfsConfig gtfs) {
        throw new IllegalStateException();
    }

    public List<? extends Trip> getRailTripsByRoute(Route route) {
        return route.getSourceGtfs().getRailTripsByRoute(route);
    }
}
