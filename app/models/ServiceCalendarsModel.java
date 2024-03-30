package models;

import configs.GtfsConfig;
import entities.LocalDateRange;
import entities.ServiceCalendar;
import entities.Trip;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ServiceCalendarsModel {

    void drop(GtfsConfig gtfs);

    ServiceCalendar create(GtfsConfig gtfs, Map<String, String> data);

    void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch);

    ServiceCalendar getByTrip(Trip trip);

    Map<String, ServiceCalendar> getByTrips(GtfsConfig gtfs, Collection<Trip> trips);

    LocalDateRange getDateRange(GtfsConfig gtfs);
}
