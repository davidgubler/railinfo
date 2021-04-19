package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.StopTime;
import models.StopTimesModel;
import services.MongoDb;

import java.util.Map;

public class MongoDbStopTimesModel implements StopTimesModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    public void drop() {
        mongoDb.get().getCollection("stopTimes").drop();
    }

    public StopTime create(Map<String, String> data) {
        StopTime stopTime = new StopTime(data);
        mongoDb.getDs().save(stopTime);
        return stopTime;
    }
}
