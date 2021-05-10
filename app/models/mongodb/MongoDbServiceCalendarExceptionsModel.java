package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import dev.morphia.InsertOptions;
import dev.morphia.query.Query;
import entities.ServiceCalendarException;
import models.ServiceCalendarExceptionsModel;
import services.MongoDb;

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
        mongoDb.getDs().save(serviceCalendarExceptions, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
        return serviceCalendarExceptions;
    }

    @Override
    public List<ServiceCalendarException> getByServiceId(String serviceId) {
        return query().field("serviceId").equal(serviceId).asList();
    }
}
