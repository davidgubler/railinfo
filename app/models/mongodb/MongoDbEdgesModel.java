package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.morphia.query.Query;
import entities.Edge;
import models.EdgesModel;
import org.bson.types.ObjectId;
import services.MongoDb;
import java.util.List;

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

    @Override
    public List<? extends Edge> getAll() {
        List<? extends Edge> edges = query().asList();
        edges.stream().forEach(edge -> injector.injectMembers(edge));
        return edges;
    }

    @Override
    public Edge get(String id) {
        return query().field("_id").equal(new ObjectId(id)).first();
    }
}
