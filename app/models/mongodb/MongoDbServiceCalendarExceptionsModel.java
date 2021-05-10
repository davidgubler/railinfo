package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.ServiceCalendarException;
import models.ServiceCalendarExceptionsModel;
import dev.morphia.query.Query;
import services.MongoDb;

import java.util.List;
import java.util.Map;

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
    public List<ServiceCalendarException> getByServiceId(String serviceId) {
        return query().field("serviceId").equal(serviceId).asList();
    }
}
