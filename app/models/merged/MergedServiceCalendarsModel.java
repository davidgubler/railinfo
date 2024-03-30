package models.merged;

import configs.GtfsConfig;
import entities.LocalDateRange;
import entities.ServiceCalendar;
import entities.Trip;
import entities.tmp.TmpLocalDateRange;
import models.ServiceCalendarsModel;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MergedServiceCalendarsModel implements ServiceCalendarsModel {
    private final List<GtfsConfig> subConfigs;

    public MergedServiceCalendarsModel(List<GtfsConfig> subConfigs) {
        this.subConfigs = subConfigs;
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        throw new IllegalStateException();
    }

    @Override
    public ServiceCalendar create(GtfsConfig gtfs, Map<String, String> data) {
        throw new IllegalStateException();
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        throw new IllegalStateException();
    }

    @Override
    public ServiceCalendar getByTrip(Trip trip) {
        return trip.getSourceGtfs().getServiceCalendarsModel().getByTrip(trip);
    }

    @Override
    public Map<String, ServiceCalendar> getByTrips(GtfsConfig gtfs, Collection<Trip> trips) {
        Map<String, ServiceCalendar> scs = new HashMap<>();
        for (GtfsConfig subCfg : subConfigs) {
            Set<Trip> tripsFromThisSubCfg = trips.stream().filter(t -> subCfg.equals(t.getSourceGtfs())).collect(Collectors.toSet());
            scs.putAll(subCfg.getServiceCalendarsModel().getByTrips(subCfg, tripsFromThisSubCfg));
        }
        return scs;
    }

    @Override
    public LocalDateRange getDateRange(GtfsConfig gtfs) {
        LocalDate maxStart = null;
        LocalDate minEnd = null;
        for (GtfsConfig subCfg : subConfigs) {
            if (maxStart == null || maxStart.isBefore(subCfg.getStart())) {
                maxStart = subCfg.getStart();
            }
            if (minEnd == null || minEnd.isAfter(subCfg.getEnd())) {
                minEnd = subCfg.getEnd();
            }
        }
        if (maxStart == null || minEnd == null) {
            return null;
        }
        return new TmpLocalDateRange(maxStart, minEnd);
    }
}
