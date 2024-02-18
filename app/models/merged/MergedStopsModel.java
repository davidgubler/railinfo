package models.merged;

import configs.GtfsConfig;
import entities.Stop;
import models.StopsModel;

import java.util.*;
import java.util.stream.Stream;

public class MergedStopsModel implements StopsModel {
    private final List<GtfsConfig> subConfigs;

    public MergedStopsModel(List<GtfsConfig> subConfigs) {
        this.subConfigs = subConfigs;
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        throw new IllegalStateException();
    }

    @Override
    public Stop create(GtfsConfig gtfs, Map<String, String> data) {
        throw new IllegalStateException();
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        throw new IllegalStateException();
    }

    @Override
    public Stop create(GtfsConfig gtfs, String name, Double lat, Double lng) {
        throw new IllegalStateException();
    }

    @Override
    public Stop create(GtfsConfig gtfs, String stopId, String name, Double lat, Double lng) {
        throw new IllegalStateException();
    }

    @Override
    public Stop get(GtfsConfig gtfs, String id) {
        for (GtfsConfig subCfg : subConfigs) {
            Stop stop = subCfg.getStopsModel().get(subCfg, id);
            if (stop != null) {
                return stop;
            }
        }
        return null;
    }

    @Override
    public Stop getByStopId(GtfsConfig gtfs, String stopId) {
        for (GtfsConfig subCfg : subConfigs) {
            Stop stop = subCfg.getStopsModel().getByStopId(subCfg, stopId);
            if (stop != null) {
                return stop;
            }
        }
        return null;
    }

    @Override
    public Stop getByStopIdUncached(GtfsConfig gtfs, String stopId) {
        throw new IllegalStateException();
    }

    @Override
    public Set<? extends Stop> getByName(GtfsConfig gtfs, String name) {
        throw new IllegalStateException();
    }

    @Override
    public Stop getPrimaryByName(GtfsConfig gtfs, String name) {
        for (GtfsConfig subCfg : subConfigs) {
            Stop stop = subCfg.getStopsModel().getPrimaryByName(subCfg, name);
            if (stop != null) {
                return stop;
            }
        }
        return null;
    }

    @Override
    public List<? extends Stop> getByPartialName(GtfsConfig gtfs, String name) {
        Set<Stop> stopsSet = new HashSet<>();
        for (GtfsConfig subCfg : subConfigs) {
            stopsSet.addAll(subCfg.getStopsModel().getByPartialName(subCfg, name));
        }
        List<Stop> stops = new LinkedList<>(stopsSet);
        Collections.sort(stops);
        return stops;
    }

    @Override
    public void updateImportance(GtfsConfig gtfs, Set<Stop> stops, Integer importance) {
        throw new IllegalStateException();
    }

    @Override
    public List<Stop> getAll(GtfsConfig gtfs) {
        throw new IllegalStateException();
    }

    @Override
    public Stream<Stop> getModified(GtfsConfig gtfs) {
        throw new IllegalStateException();
    }

    @Override
    public void update(GtfsConfig gtfs, Stop stop, String name, Double lat, Double lng) {
        throw new IllegalStateException();
    }

    @Override
    public void delete(GtfsConfig gtfs, Stop stop) {
        throw new IllegalStateException();
    }
}
