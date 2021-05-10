package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.ServiceCalendar;
import entities.Stop;
import models.ServiceCalendarsModel;
import dev.morphia.query.Query;
import services.MongoDb;

import java.util.Map;

public class MongoDbServiceCalendarsModel implements ServiceCalendarsModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    private Query<ServiceCalendar> query() {
        return mongoDb.getDs().createQuery(ServiceCalendar.class);
    }

    @Override
    public void drop() {
        mongoDb.get().getCollection("serviceCalendars").drop();
    }

    @Override
    public ServiceCalendar create(Map<String, String> data) {
        ServiceCalendar serviceCalendar = new ServiceCalendar(data);
        mongoDb.getDs().save(serviceCalendar);
        return serviceCalendar;
    }

    @Override
    public ServiceCalendar getByServiceId(String serviceId) {
        return query().field("serviceId").equal(serviceId).get();
    }
}
