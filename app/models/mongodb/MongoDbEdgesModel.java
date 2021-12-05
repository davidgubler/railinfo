package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.morphia.query.Query;
import entities.Edge;
import models.EdgesModel;
import services.MongoDb;

public class MongoDbEdgesModel implements EdgesModel {
    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    private Query<Edge> query() {
        return mongoDb.getDs().createQuery(Edge.class);
    }

    @Override
    public void drop() {
        mongoDb.get().getCollection("edges").drop();
    }

    @Override
    public Edge save(Edge edge) {
        mongoDb.getDs().save(edge);
        return edge;
    }

}
