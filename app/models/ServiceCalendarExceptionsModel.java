package models;

import configs.GtfsConfig;
import entities.LocalDateRange;
import entities.ServiceCalendarException;
import entities.Trip;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ServiceCalendarExceptionsModel {

    void drop(GtfsConfig gtfs);

    ServiceCalendarException create(GtfsConfig gtfs, Map<String, String> data);

    void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch);

    List<? extends ServiceCalendarException> getByTrip(Trip trip);

    Map<String, List<ServiceCalendarException>> getByTripsAndDates(GtfsConfig gtfs, Collection<Trip> trips, Collection<LocalDate> dates);

    LocalDateRange getDateRange(GtfsConfig gtfs);
}
