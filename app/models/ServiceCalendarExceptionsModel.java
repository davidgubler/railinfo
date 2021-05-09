package models;

import entities.ServiceCalendarException;

import java.util.List;
import java.util.Map;

public interface ServiceCalendarExceptionsModel {

    void drop();

    ServiceCalendarException create(Map<String, String> data);

    List<ServiceCalendarException> create(List<Map<String, String>> dataBatch);

    List<ServiceCalendarException> getByServiceId(String serviceId);
}
