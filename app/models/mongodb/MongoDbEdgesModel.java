package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import entities.Edge;
import entities.ReverseEdge;
import entities.Stop;
import entities.mongodb.MongoDbEdge;
import geometry.EdgeSpreadComparator;
import geometry.Point;
import models.EdgesModel;
import models.StopsModel;
import org.bson.types.ObjectId;
import services.MongoDb;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MongoDbEdgesModel implements EdgesModel {
    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    @Inject
    private StopsModel stopsModel;

    private Query<MongoDbEdge> query(String databaseName) {
        return mongoDb.getDs(databaseName).createQuery(MongoDbEdge.class);
    }

    private Query<MongoDbEdge> queryId(String databaseName, ObjectId objectId) {
        return query(databaseName).field("_id").equal(objectId);
    }

    private UpdateOperations<MongoDbEdge> ops(String databaseName) {
        return mongoDb.getDs(databaseName).createUpdateOperations(MongoDbEdge.class);
    }

    @Override
    public void drop(String databaseName) {
        mongoDb.get(databaseName).getCollection("edges").drop();
    }

    @Override
    public Edge save(String databaseName, Edge edge) {
        mongoDb.getDs(databaseName).save(edge);
        return edge;
    }

    @Override
    public List<? extends Edge> getAll(String databaseName) {
        List<? extends MongoDbEdge> edges = query(databaseName).find().toList();
        edges.stream().forEach(edge -> { injector.injectMembers(edge); edge.setDatabaseName(databaseName); });
        return edges;
    }

    @Override
    public List<? extends Edge> getModified(String databaseName) {
        List<? extends MongoDbEdge> edges = query(databaseName).field("modified").equal(true).find().toList();
        edges.stream().forEach(edge -> { injector.injectMembers(edge); edge.setDatabaseName(databaseName); });
        return edges;
    }

    @Override
    public Edge get(String databaseName, String id) {
        ObjectId objectId;
        boolean reverse;
        try {
            reverse = id.startsWith("-");
            if (reverse) {
                id = id.substring(1);
            }
            objectId = new ObjectId(id);
        } catch (Exception e) {
            return null;
        }
        MongoDbEdge edge = query(databaseName).field("_id").equal(objectId).first();

        if (edge == null) {
            return null;
        }
        if (edge != null) {
            injector.injectMembers(edge);
            edge.setDatabaseName(databaseName);

        }
        return reverse ? new ReverseEdge(edge) : edge;
    }

    @Override
    public Edge create(String databaseName, Stop stop1, Stop stop2, Integer typicalTime) {
        return create(databaseName, stop1.getBaseId(), stop2.getBaseId(), typicalTime);
    }

    @Override
    public Edge create(String databaseName, String stop1Id, String stop2Id, Integer typicalTime) {
        MongoDbEdge edge = new MongoDbEdge(stopsModel, databaseName, stop1Id, stop2Id, typicalTime, true);
        mongoDb.getDs(databaseName).save(edge);
        edge.setDatabaseName(databaseName);
        return edge;
    }

    @Override
    public void update(String databaseName, Edge edge, Integer typicalTime) {
        MongoDbEdge mongoDbEdge = (MongoDbEdge)edge;
        mongoDbEdge.setTypicalTime(typicalTime);
        mongoDbEdge.setModified(true);
        mongoDb.getDs(databaseName).update(queryId(databaseName, mongoDbEdge.getObjectId()), ops(databaseName).set("typicalTime", mongoDbEdge.getTypicalTime()).set("modified", Boolean.TRUE));
    }

    @Override
    public void delete(String databaseName, Edge edge) {
        MongoDbEdge mongoDbEdge = (MongoDbEdge)edge;
        mongoDb.getDs(databaseName).delete(queryId(databaseName, mongoDbEdge.getObjectId()));
    }

    @Override
    public List<? extends Edge> getEdgesFrom(String databaseName, Stop stop) {
        String stopId = stop.getBaseId();

        Query<MongoDbEdge> query = query(databaseName);
        query.or(query.or(query.criteria("stop1Id").equal(stopId), query.criteria("stop2Id").equal(stopId)));
        List<MongoDbEdge> edges = query.asList();
        for(MongoDbEdge edge : edges) {
            injector.injectMembers(edge);
            edge.setDatabaseName(databaseName);
        }
        return edges;
    }

    @Override
    public List<? extends Edge> getByPoint(String databaseName, Point point) {
        Query<MongoDbEdge> query = query(databaseName);
        List<? extends Edge> edges = query.field("bbNorth").greaterThan(point.getLat()).field("bbSouth").lessThan(point.getLat()).field("bbEast").greaterThan(point.getLng()).field("bbWest").lessThan(point.getLng()).asList();
        edges.stream().forEach(e -> { injector.injectMembers(e); ((MongoDbEdge)e).setDatabaseName(databaseName); });

        if (edges.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.sort(edges, new EdgeSpreadComparator(point));

        List<Edge> finalEdges = new LinkedList<>();

        double lastSpread = edges.get(0).getSpread(point);
        for (Edge edge : edges) {
            if (Math.abs(lastSpread - edge.getSpread(point)) > 30) {
                break;
            }
            finalEdges.add(edge);
        }

        return finalEdges;
    }
}
