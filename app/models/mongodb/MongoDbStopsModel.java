package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.morphia.query.experimental.filters.Filters;
import entities.ServiceCalendarException;
import entities.Stop;
import models.StopsModel;
import dev.morphia.query.Query;
import services.MongoDb;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MongoDbStopsModel implements StopsModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    public void drop() {
        mongoDb.get().getCollection("stops").drop();
    }

    public Stop create(Map<String, String> data) {
        Stop stop = new Stop(data);
        mongoDb.getDs().save(stop);
        return stop;
    }

    public Set<Stop> getByName(String name) {
        Set<Stop> stops = new HashSet<>();
        stops.addAll(mongoDb.getDs().find(Stop.class).filter(Filters.eq("name", name)).iterator().toList());
        System.out.println("===1=== " + stops);
        return stops;
    }
}
