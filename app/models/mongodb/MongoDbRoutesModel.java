package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
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

    @Inject
    private MongoDb mongoDb;

    private Query<Route> query(String databaseName) {
        return mongoDb.getDs(databaseName).createQuery(Route.class);
    }

    @Override
    public void drop(String databaseName) {
        mongoDb.get(databaseName).getCollection("routes").drop();
    }

    @Override
    public Route create(String databaseName, Map<String, String> data) {
        Route route = new Route(data);
        mongoDb.getDs(databaseName).save(route);
        return route;
    }

    @Override
    public void create(String databaseName, List<Map<String, String>> dataBatch) {
        List<Route> routes = dataBatch.stream().map(data -> new Route(data)).collect(Collectors.toList());
        mongoDb.getDs(databaseName).save(routes, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
    }

    @Override
    public Route getByRouteId(String databaseName, String id) {
        Route route = query(databaseName).field("routeId").equal(id).get();
        return route;
    }

    @Override
    public List<Route> getByType(String databaseName, int from, int to) {
        return query(databaseName).field("type").greaterThanOrEq(from).field("type").lessThanOrEq(to).asList();
    }
}
