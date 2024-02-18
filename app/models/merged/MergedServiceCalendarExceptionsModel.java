package models.merged;

import configs.GtfsConfig;
import entities.ServiceCalendar;
import entities.ServiceCalendarException;
import entities.Trip;
import models.ServiceCalendarExceptionsModel;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MergedServiceCalendarExceptionsModel implements ServiceCalendarExceptionsModel {
    private final List<GtfsConfig> subConfigs;

    public MergedServiceCalendarExceptionsModel(List<GtfsConfig> subConfigs) {
        this.subConfigs = subConfigs;
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        throw new IllegalStateException();
    }

    @Override
    public ServiceCalendarException create(GtfsConfig gtfs, Map<String, String> data) {
        throw new IllegalStateException();
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        throw new IllegalStateException();
    }

    @Override
    public List<? extends ServiceCalendarException> getByTrip(Trip trip) {
        return trip.getSourceGtfs().getServiceCalendarExceptionsModel().getByTrip(trip);
    }

    @Override
    public Map<String, List<ServiceCalendarException>> getByTripsAndDates(GtfsConfig gtfs, Collection<Trip> trips, Collection<LocalDate> dates) {
        Map<String, List<ServiceCalendarException>> sces = new HashMap<>();
        for (GtfsConfig subCfg : subConfigs) {
            Set<Trip> tripsFromThisSubCfg = trips.stream().filter(t -> subCfg.equals(t.getSourceGtfs())).collect(Collectors.toSet());
            sces.putAll(subCfg.getServiceCalendarExceptionsModel().getByTripsAndDates(subCfg, tripsFromThisSubCfg, dates));
        }
        return sces;
    }
}
