package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import dev.morphia.InsertOptions;
import entities.ServiceCalendar;
import models.ServiceCalendarsModel;
import dev.morphia.query.Query;
import services.MongoDb;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<ServiceCalendar> create(List<Map<String, String>> dataBatch) {
        List<ServiceCalendar> serviceCalendarExceptions = dataBatch.stream().map(data -> new ServiceCalendar(data)).collect(Collectors.toList());
        mongoDb.getDs().save(serviceCalendarExceptions, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
        return serviceCalendarExceptions;
    }

    @Override
    public ServiceCalendar getByServiceId(String serviceId) {
        return query().field("serviceId").equal(serviceId).get();
    }
}
