package models;

import entities.ServiceCalendar;

import java.util.List;
import java.util.Map;

public interface ServiceCalendarsModel {

    void drop(String databaseName);

    ServiceCalendar create(String databaseName, Map<String, String> data);

    List<ServiceCalendar> create(String databaseName, List<Map<String, String>> dataBatch);

    ServiceCalendar getByServiceId(String databaseName, String serviceId);
}
