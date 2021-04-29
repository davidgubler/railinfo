package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.Stop;
import models.StopsModel;
import org.mongodb.morphia.query.Query;
import services.MongoDb;

import java.util.Map;

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

    public Stop getByName(String name) {
        return query().field("name").equal(name).get();
    }
}
