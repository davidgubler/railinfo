package models;

import entities.ServiceCalendar;
import entities.Trip;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ServiceCalendarsModel {

    void drop(String databaseName);

    ServiceCalendar create(String databaseName, Map<String, String> data);

    void create(String databaseName, List<Map<String, String>> dataBatch);

    ServiceCalendar getByServiceId(String databaseName, String serviceId);

    Map<String, ServiceCalendar> getByTrips(String databaseName, Collection<Trip> trips);
}
