package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.Stop;
import models.StopsModel;
import org.mongodb.morphia.query.Query;
import services.MongoDb;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MongoDbStopsModel implements StopsModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    private Query<Stop> query() {
        return mongoDb.getDs().createQuery(Stop.class);
    }

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
        stops.addAll(query().field("name").equal(name).asList());
        System.out.println("===1=== " + stops);
        return stops;
    }
}
