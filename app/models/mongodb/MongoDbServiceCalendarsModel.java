package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.ServiceCalendar;
import models.ServiceCalendarsModel;
import services.MongoDb;

import java.util.Map;

public class MongoDbServiceCalendarsModel implements ServiceCalendarsModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    public void drop() {
        mongoDb.get().getCollection("serviceCalendars").drop();
    }

    public ServiceCalendar create(Map<String, String> data) {
        ServiceCalendar serviceCalendar = new ServiceCalendar(data);
        mongoDb.getDs().save(serviceCalendar);
        return serviceCalendar;
    }
}
