package models;

import entities.ServiceCalendar;

import java.util.Map;

public interface ServiceCalendarsModel {
    void drop();
    ServiceCalendar create(Map<String, String> data);
}
