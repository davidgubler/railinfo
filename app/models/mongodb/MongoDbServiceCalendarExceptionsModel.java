package models.mongodb;

import com.google.inject.Inject;
import com.mongodb.WriteConcern;
import dev.morphia.InsertOptions;
import dev.morphia.query.Query;
import entities.ServiceCalendarException;
import entities.Trip;
import models.ServiceCalendarExceptionsModel;
import services.MongoDb;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MongoDbServiceCalendarExceptionsModel implements ServiceCalendarExceptionsModel {

    @Inject
    private MongoDb mongoDb;

    private Query<ServiceCalendarException> query(String databaseName) {
        return mongoDb.getDs(databaseName).createQuery(ServiceCalendarException.class);
    }

    @Override
    public void drop(String databaseName) {
        mongoDb.get(databaseName).getCollection("serviceCalendarExceptions").drop();
    }

    @Override
    public ServiceCalendarException create(String databaseName, Map<String, String> data) {
        ServiceCalendarException serviceCalendarException = new ServiceCalendarException(data);
        mongoDb.getDs(databaseName).save(serviceCalendarException);
        return serviceCalendarException;
    }

    @Override
    public void create(String databaseName, List<Map<String, String>> dataBatch) {
        List<ServiceCalendarException> serviceCalendarExceptions = dataBatch.stream().map(data -> new ServiceCalendarException(data)).collect(Collectors.toList());
        mongoDb.getDs(databaseName).save(serviceCalendarExceptions, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
    }

    @Override
    public List<ServiceCalendarException> getByServiceId(String databaseName, String serviceId) {
        return query(databaseName).field("serviceId").equal(serviceId).asList();
    }

    @Override
    public Map<String, List<ServiceCalendarException>> getByTripsAndDates(String databaseName, Collection<Trip> trips, Collection<LocalDate> localDates) {
        Set<String> serviceIds = trips.stream().map(Trip::getServiceId).collect(Collectors.toSet());
        Set<String> dates = localDates.stream().map(LocalDate::toString).collect(Collectors.toSet());
        Map<String, List<ServiceCalendarException>> exceptionsByServiceId = new HashMap<>();
        for (ServiceCalendarException sce : query(databaseName).field("serviceId").in(serviceIds).field("date").in(dates).asList() ) {
            if (!exceptionsByServiceId.containsKey(sce.getServiceId())) {
                exceptionsByServiceId.put(sce.getServiceId(), new LinkedList<>());
            }
            exceptionsByServiceId.get(sce.getServiceId()).add(sce);
        }
        return exceptionsByServiceId;
    }
}
