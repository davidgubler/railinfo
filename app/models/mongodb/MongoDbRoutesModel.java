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

    private Query<Route> query() {
        return mongoDb.getDs().createQuery(Route.class);
    }

    @Override
    public void drop() {
        mongoDb.get().getCollection("routes").drop();
    }

    @Override
    public Route create(Map<String, String> data) {
        Route route = new Route(data);
        mongoDb.getDs().save(route);
        return route;
    }

    @Override
    public List<Route> create(List<Map<String, String>> dataBatch) {
        List<Route> routes = dataBatch.stream().map(data -> new Route(data)).collect(Collectors.toList());
        mongoDb.getDs().save(routes, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
        return routes;
    }

    @Override
    public Route getByRouteId(String id) {
        Route route = query().field("routeId").equal(id).get();
        injector.injectMembers(route);
        return route;
    }
}
