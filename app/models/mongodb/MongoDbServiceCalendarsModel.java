package models.mongodb;

import com.mongodb.WriteConcern;
import configs.GtfsConfig;
import dev.morphia.InsertOptions;
import entities.ServiceCalendar;
import entities.mongodb.MongoDbServiceCalendar;
import entities.Trip;
import models.ServiceCalendarsModel;
import dev.morphia.query.Query;

import java.util.*;
import java.util.stream.Collectors;

public class MongoDbServiceCalendarsModel implements ServiceCalendarsModel {

    private Query<MongoDbServiceCalendar> query(GtfsConfig gtfs) {
        return gtfs.getDs().createQuery(MongoDbServiceCalendar.class);
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        gtfs.getDatabase().getCollection("serviceCalendars").drop();
    }

    @Override
    public ServiceCalendar create(GtfsConfig gtfs, Map<String, String> data) {
        ServiceCalendar serviceCalendar = new MongoDbServiceCalendar(data);
        gtfs.getDs().save(serviceCalendar);
        return serviceCalendar;
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        List<ServiceCalendar> serviceCalendarExceptions = dataBatch.stream().map(data -> new MongoDbServiceCalendar(data)).collect(Collectors.toList());
        gtfs.getDs().save(serviceCalendarExceptions, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
    }

    @Override
    public ServiceCalendar getByServiceId(GtfsConfig gtfs, String serviceId) {
        return query(gtfs).field("serviceId").equal(serviceId).get();
    }

    @Override
    public Map<String, ServiceCalendar> getByTrips(GtfsConfig gtfs, Collection<Trip> trips) {
        Set<String> serviceIds = trips.stream().map(Trip::getServiceId).collect(Collectors.toSet());
        Map<String, ServiceCalendar> serviceCalendarByServiceId = new HashMap<>();
        for (ServiceCalendar serviceCalendar : query(gtfs).field("serviceId").in(serviceIds).asList() ) {
            serviceCalendarByServiceId.put(serviceCalendar.getServiceId(), serviceCalendar);
        }
        return serviceCalendarByServiceId;
    }
}
