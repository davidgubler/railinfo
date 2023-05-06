package models;

import entities.ServiceCalendarException;
import entities.Trip;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ServiceCalendarExceptionsModel {

    void drop(String databaseName);

    ServiceCalendarException create(String databaseName, Map<String, String> data);

    void create(String databaseName, List<Map<String, String>> dataBatch);

    List<ServiceCalendarException> getByServiceId(String databaseName, String serviceId);

    Map<String, List<ServiceCalendarException>> getByTripsAndDates(String databaseName, Collection<Trip> trips, Collection<LocalDate> dates);
}
