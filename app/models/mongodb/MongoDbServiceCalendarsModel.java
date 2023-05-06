package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import dev.morphia.InsertOptions;
import entities.ServiceCalendar;
import entities.ServiceCalendarException;
import entities.Trip;
import models.ServiceCalendarsModel;
import dev.morphia.query.Query;
import services.MongoDb;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MongoDbServiceCalendarsModel implements ServiceCalendarsModel {

    @Inject
    private MongoDb mongoDb;

    private Query<ServiceCalendar> query(String databaseName) {
        return mongoDb.getDs(databaseName).createQuery(ServiceCalendar.class);
    }

    @Override
    public void drop(String databaseName) {
        mongoDb.get(databaseName).getCollection("serviceCalendars").drop();
    }

    @Override
    public ServiceCalendar create(String databaseName, Map<String, String> data) {
        ServiceCalendar serviceCalendar = new ServiceCalendar(data);
        mongoDb.getDs(databaseName).save(serviceCalendar);
        return serviceCalendar;
    }

    @Override
    public void create(String databaseName, List<Map<String, String>> dataBatch) {
        List<ServiceCalendar> serviceCalendarExceptions = dataBatch.stream().map(data -> new ServiceCalendar(data)).collect(Collectors.toList());
        mongoDb.getDs(databaseName).save(serviceCalendarExceptions, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
    }

    @Override
    public ServiceCalendar getByServiceId(String databaseName, String serviceId) {
        return query(databaseName).field("serviceId").equal(serviceId).get();
    }

    @Override
    public Map<String, ServiceCalendar> getByTrips(String databaseName, Collection<Trip> trips) {
        Set<String> serviceIds = trips.stream().map(Trip::getServiceId).collect(Collectors.toSet());
        Map<String, ServiceCalendar> serviceCalendarByServiceId = new HashMap<>();
        for (ServiceCalendar serviceCalendar : query(databaseName).field("serviceId").in(serviceIds).asList() ) {
            serviceCalendarByServiceId.put(serviceCalendar.getServiceId(), serviceCalendar);
        }
        return serviceCalendarByServiceId;
    }
}
