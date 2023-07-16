package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import configs.GtfsConfig;
import dev.morphia.InsertOptions;
import dev.morphia.query.Query;
import entities.Route;
import models.RoutesModel;
import services.MongoDb;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoDbRoutesModel implements RoutesModel {

    @Inject
    private Injector injector;

    private Query<Route> query(GtfsConfig gtfs) {
        return gtfs.getDs().createQuery(Route.class);
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        gtfs.getDatabase().getCollection("routes").drop();
    }

    @Override
    public Route create(GtfsConfig gtfs, Map<String, String> data) {
        Route route = new Route(data);
        gtfs.getDs().save(route);
        return route;
    }

    @Override
    public void create(GtfsConfig gtfs, List<Map<String, String>> dataBatch) {
        List<Route> routes = dataBatch.stream().map(data -> new Route(data)).collect(Collectors.toList());
        gtfs.getDs().save(routes, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
    }

    @Override
    public Route getByRouteId(GtfsConfig gtfs, String id) {
        Route route = query(gtfs).field("routeId").equal(id).get();
        return route;
    }

    @Override
    public List<Route> getByType(GtfsConfig gtfs, int from, int to) {
        return query(gtfs).field("type").greaterThanOrEq(from).field("type").lessThanOrEq(to).asList();
    }
}
