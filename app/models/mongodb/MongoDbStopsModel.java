package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.Stop;
import models.StopsModel;
import services.MongoDb;

import java.util.Map;

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
}
