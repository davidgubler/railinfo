package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import entities.Edge;
import entities.Stop;
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
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (Exception e) {
            return null;
        }
        Edge edge = query().field("_id").equal(objectId).first();
        if (edge != null) {
            injector.injectMembers(edge);
        }
        return edge;
    }

    @Override
    public Edge create(Stop stop1, Stop stop2, Integer typicalTime) {
        MongoDbEdge edge = new MongoDbEdge(stop1.getBaseId(), stop2.getBaseId(), typicalTime, true);
        mongoDb.getDs(Config.TIMETABLE_DB).save(edge);
        injector.injectMembers(edge);
        return edge;
    }

    @Override
    public void update(Edge edge, Integer typicalTime) {
        MongoDbEdge mongoDbEdge = (MongoDbEdge)edge;
        mongoDbEdge.setTypicalTime(typicalTime);
        mongoDbEdge.setModified(true);
        mongoDb.getDs(Config.TIMETABLE_DB).update(queryId(mongoDbEdge.getObjectId()), ops().set("typicalTime", mongoDbEdge.getTypicalTime()).set("modified", Boolean.TRUE));
    }

    @Override
    public void delete(Edge edge) {
        MongoDbEdge mongoDbEdge = (MongoDbEdge)edge;
        mongoDb.getDs(Config.TIMETABLE_DB).delete(queryId(mongoDbEdge.getObjectId()));
    }

    @Override
    public List<? extends Edge> getEdgesFrom(Stop stop) {
        String stopId = stop.getBaseId();

        Query<MongoDbEdge> query = query();
        query.or(query.or(query.criteria("stop1Id").equal(stopId), query.criteria("stop2Id").equal(stopId)));
        List<MongoDbEdge> edges = query.asList();
        for(Edge edge : edges) {
            injector.injectMembers(edge);
        }
        return edges;
    }
}
