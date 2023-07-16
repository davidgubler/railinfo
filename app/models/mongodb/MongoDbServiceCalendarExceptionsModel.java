package models.mongodb;

import com.mongodb.WriteConcern;
import configs.GtfsConfig;
import dev.morphia.InsertOptions;
import dev.morphia.query.Query;
import entities.ServiceCalendarException;
import entities.Trip;
import models.ServiceCalendarExceptionsModel;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MongoDbServiceCalendarExceptionsModel implements ServiceCalendarExceptionsModel {

    private Query<ServiceCalendarException> query(GtfsConfig gtfs) {
        return gtfs.getDs().createQuery(ServiceCalendarException.class);
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        gtfs.getDatabase().getCollection("serviceCalendarExceptions").drop();
    }

    @Override
    public ServiceCalendarException create(GtfsConfig gtfs, Map<String, String> data) {
        ServiceCalendarException serviceCalendarException = new ServiceCalendarException(data);
        gtfs.getDs().save(serviceCalendarException);
        return serviceCalendarException;
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        List<ServiceCalendarException> serviceCalendarExceptions = dataBatch.stream().map(data -> new ServiceCalendarException(data)).collect(Collectors.toList());
        gtfs.getDs().save(serviceCalendarExceptions, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
    }

    @Override
    public List<ServiceCalendarException> getByServiceId(GtfsConfig gtfs, String serviceId) {
        return query(gtfs).field("serviceId").equal(serviceId).asList();
    }

    @Override
    public Map<String, List<ServiceCalendarException>> getByTripsAndDates(GtfsConfig gtfs, Collection<Trip> trips, Collection<LocalDate> localDates) {
        Set<String> serviceIds = trips.stream().map(Trip::getServiceId).collect(Collectors.toSet());
        Set<String> dates = localDates.stream().map(LocalDate::toString).collect(Collectors.toSet());
        Map<String, List<ServiceCalendarException>> exceptionsByServiceId = new HashMap<>();
        for (ServiceCalendarException sce : query(gtfs).field("serviceId").in(serviceIds).field("date").in(dates).asList() ) {
            if (!exceptionsByServiceId.containsKey(sce.getServiceId())) {
                exceptionsByServiceId.put(sce.getServiceId(), new LinkedList<>());
            }
            exceptionsByServiceId.get(sce.getServiceId()).add(sce);
        }
        return exceptionsByServiceId;
    }
}
