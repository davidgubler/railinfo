package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import entities.Edge;
import entities.mongodb.MongoDbEdge;
import models.EdgesModel;
import org.bson.types.ObjectId;
import services.MongoDb;
import utils.Config;

import java.util.List;

public class MongoDbEdgesModel implements EdgesModel {
    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    private Query<MongoDbEdge> query() {
        return mongoDb.getDs(Config.TIMETABLE_DB).createQuery(MongoDbEdge.class);
    }

    private Query<MongoDbEdge> queryId(ObjectId objectId) {
        return query().field("_id").equal(objectId);
    }

    private UpdateOperations<MongoDbEdge> ops() {
        return mongoDb.getDs(Config.TIMETABLE_DB).createUpdateOperations(MongoDbEdge.class);
    }

    @Override
    public void drop() {
        mongoDb.get(Config.TIMETABLE_DB).getCollection("edges").drop();
    }

    @Override
    public Edge save(Edge edge) {
        mongoDb.getDs(Config.TIMETABLE_DB).save(edge);
        return edge;
    }

    @Override
    public List<? extends Edge> getAll() {
        List<? extends MongoDbEdge> edges = query().asList();
        edges.stream().forEach(edge -> injector.injectMembers(edge));
        return edges;
    }

    @Override
    public Edge get(String id) {
        Edge edge = queryId(new ObjectId(id)).first();
        injector.injectMembers(edge);
        return edge;
    }

    @Override
    public void update(Edge edge, int typicalTime) {
        MongoDbEdge mongoDbEdge = (MongoDbEdge)edge;
        mongoDbEdge.setTypicalTime(typicalTime);
        mongoDb.getDs(Config.TIMETABLE_DB).update(queryId(mongoDbEdge.getObjectId()), ops().set("typicalTime", mongoDbEdge.getTypicalTime()));
    }

    @Override
    public void delete(Edge edge) {
        MongoDbEdge mongoDbEdge = (MongoDbEdge)edge;
        mongoDb.getDs(Config.TIMETABLE_DB).delete(queryId(mongoDbEdge.getObjectId()));
    }
}
