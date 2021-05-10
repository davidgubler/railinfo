package models;

import entities.ServiceCalendar;
import entities.ServiceCalendarException;

import java.util.List;
import java.util.Map;

public interface ServiceCalendarsModel {

    void drop();

    ServiceCalendar create(Map<String, String> data);

    List<ServiceCalendar> create(List<Map<String, String>> dataBatch);

    ServiceCalendar getByServiceId(String serviceId);
}
