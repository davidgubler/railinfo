package models;

import entities.ServiceCalendarException;

import java.util.List;
import java.util.Map;

public interface ServiceCalendarExceptionsModel {

    void drop(String databaseName);

    ServiceCalendarException create(String databaseName, Map<String, String> data);

    void create(String databaseName, List<Map<String, String>> dataBatch);

    List<ServiceCalendarException> getByServiceId(String databaseName, String serviceId);
}
