package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.WriteConcern;
import dev.morphia.InsertOptions;
import dev.morphia.query.Query;
import entities.Route;
import models.RoutesModel;
import services.MongoDb;
import utils.Config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoDbRoutesModel implements RoutesModel {

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    private Query<Route> query() {
        return mongoDb.getDs(Config.TIMETABLE_DB).createQuery(Route.class);
    }

    @Override
    public void drop() {
        mongoDb.get(Config.TIMETABLE_DB).getCollection("routes").drop();
    }

    @Override
    public Route create(Map<String, String> data) {
        Route route = new Route(data);
        mongoDb.getDs(Config.TIMETABLE_DB).save(route);
        return route;
    }

    @Override
    public List<Route> create(List<Map<String, String>> dataBatch) {
        List<Route> routes = dataBatch.stream().map(data -> new Route(data)).collect(Collectors.toList());
        mongoDb.getDs(Config.TIMETABLE_DB).save(routes, new InsertOptions().writeConcern(WriteConcern.UNACKNOWLEDGED));
        return routes;
    }

    @Override
    public Route getByRouteId(String id) {
        Route route = query().field("routeId").equal(id).get();
        injector.injectMembers(route);
        return route;
    }

    @Override
    public List<Route> getByType(int from, int to) {
        return query().field("type").greaterThanOrEq(from).field("type").lessThanOrEq(to).asList();
    }
}
