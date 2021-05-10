package models;

import entities.ServiceCalendar;
import entities.Stop;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StopsModel {
    void drop();

    Stop create(Map<String, String> data);

    List<Stop> create(List<Map<String, String>> dataBatch);

    Set<Stop> getByName(String name);
}
