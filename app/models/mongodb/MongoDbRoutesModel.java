package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import configs.GtfsConfig;
import dev.morphia.InsertOptions;
import dev.morphia.query.Query;
import entities.mongodb.MongoDbRoute;
import models.RoutesModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoDbRoutesModel implements RoutesModel {

    @Inject
    private Injector injector;

    private Query<MongoDbRoute> query(GtfsConfig gtfs) {
        return gtfs.getDs().createQuery(MongoDbRoute.class);
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        gtfs.getDatabase().getCollection("routes").drop();
    }

    @Override
    public MongoDbRoute create(GtfsConfig gtfs, Map<String, String> data) {
        MongoDbRoute route = new MongoDbRoute(data);
        gtfs.getDs().save(route);
        return route;
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        List<MongoDbRoute> routes = dataBatch.stream().map(data -> new MongoDbRoute(data)).collect(Collectors.toList());
        gtfs.getDs().save(routes, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
    }

    @Override
    public MongoDbRoute getByRouteId(GtfsConfig gtfs, String id) {
        MongoDbRoute route = query(gtfs).field("routeId").equal(id).get();
        return route;
    }

    @Override
    public List<MongoDbRoute> getByType(GtfsConfig gtfs, int min, int max) {
        return query(gtfs).field("type").greaterThanOrEq(min).field("type").lessThanOrEq(max).asList();
    }
}
