package models.merged;

import configs.GtfsConfig;
import entities.Stop;
import entities.StopTime;
import entities.Trip;
import models.StopTimesModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergedStopTimesModel implements StopTimesModel {

    private final List<GtfsConfig> subConfigs;

    public MergedStopTimesModel(List<GtfsConfig> subConfigs) {
        this.subConfigs = subConfigs;
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        throw new IllegalStateException();
    }

    @Override
    public StopTime create(GtfsConfig gtfs, Map<String, String> data) {
        throw new IllegalStateException();
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        throw new IllegalStateException();
    }

    @Override
    public List<? extends StopTime> getByStops(GtfsConfig gtfs, Collection<? extends Stop> stops) {
        throw new IllegalStateException();
    }

    @Override
    public List<? extends StopTime> getByTrip(GtfsConfig gtfs, Trip trip) {
        throw new IllegalStateException();
    }

    @Override
    public Map<Trip, List<StopTime>> getByTrips(GtfsConfig gtfs, List<? extends Trip> trips) {
        Map<Trip, List<StopTime>> stopTimes = new HashMap<>();
        for (GtfsConfig subCfg : subConfigs) {
            stopTimes.putAll(subCfg.getStopTimesModel().getByTrips(subCfg, trips));
        }
        return stopTimes;
    }
}
