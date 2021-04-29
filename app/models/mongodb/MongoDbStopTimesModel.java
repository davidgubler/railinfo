package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.Stop;
import entities.StopTime;
import models.StopTimesModel;
import org.mongodb.morphia.query.Query;
import services.MongoDb;

import java.util.List;
import java.util.Map;

public class MongoDbStopTimesModel implements StopTimesModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    private Query<StopTime> query() {
        return mongoDb.getDs().createQuery(StopTime.class);
    }

    @Override
    public void drop() {
        mongoDb.get().getCollection("stopTimes").drop();
    }

    @Override
    public StopTime create(Map<String, String> data) {
        StopTime stopTime = new StopTime(data);
        mongoDb.getDs().save(stopTime);
        return stopTime;
    }

    @Override
    public List<StopTime> getByStop(Stop stop) {
        return query().field("stopId").equal(stop.getStopId()).asList();
    }
}
