package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.InsertOptions;
import com.mongodb.WriteConcern;
import dev.morphia.InsertManyOptions;
import dev.morphia.query.experimental.filters.Filters;
import entities.ServiceCalendarException;
import models.ServiceCalendarExceptionsModel;
import services.MongoDb;
import dev.morphia.query.Query;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoDbServiceCalendarExceptionsModel implements ServiceCalendarExceptionsModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    private Query<ServiceCalendarException> query() {
        return mongoDb.getDs().createQuery(ServiceCalendarException.class);
    }

    @Override
    public void drop() {
        mongoDb.get().getCollection("serviceCalendarExceptions").drop();
    }

    @Override
    public ServiceCalendarException create(Map<String, String> data) {
        ServiceCalendarException serviceCalendarException = new ServiceCalendarException(data);
        mongoDb.getDs().save(serviceCalendarException);
        return serviceCalendarException;
    }

    @Override
    public List<ServiceCalendarException> create(List<Map<String, String>> dataBatch) {
        List<ServiceCalendarException> serviceCalendarExceptions = dataBatch.stream().map(data -> new ServiceCalendarException(data)).collect(Collectors.toList());
        InsertManyOptions options = new InsertManyOptions();
        options.bypassDocumentValidation(true);
        options.writeConcern(WriteConcern.UNACKNOWLEDGED);
        mongoDb.getDs().save(serviceCalendarExceptions, options);
        return serviceCalendarExceptions;
    }

    @Override
    public List<ServiceCalendarException> getByServiceId(String serviceId) {
        return mongoDb.getDs().find(ServiceCalendarException.class).filter(Filters.eq("serviceId", serviceId)).iterator().toList();
    }
}
