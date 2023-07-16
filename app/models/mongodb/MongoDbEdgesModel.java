package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import configs.GtfsConfig;
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
    private StopsModel stopsModel;

    private Query<MongoDbEdge> query(GtfsConfig gtfs) {
        return gtfs.getDs().createQuery(MongoDbEdge.class);
    }

    private Query<MongoDbEdge> queryId(GtfsConfig gtfs, ObjectId objectId) {
        return query(gtfs).field("_id").equal(objectId);
    }

    private UpdateOperations<MongoDbEdge> ops(GtfsConfig gtfs) {
        return gtfs.getDs().createUpdateOperations(MongoDbEdge.class);
    }

    @Override
    public void drop(GtfsConfig gtfs) {
        gtfs.getDatabase().getCollection("edges").drop();
    }

    @Override
    public Edge save(GtfsConfig gtfs, Edge edge) {
        gtfs.getDs().save(edge);
        return edge;
    }

    @Override
    public List<? extends Edge> getAll(GtfsConfig gtfs) {
        List<? extends MongoDbEdge> edges = query(gtfs).find().toList();
        edges.stream().forEach(edge -> { injector.injectMembers(edge); edge.setGtfs(gtfs); });
        return edges;
    }

    @Override
    public List<? extends Edge> getModified(GtfsConfig gtfs) {
        List<? extends MongoDbEdge> edges = query(gtfs).field("modified").equal(true).find().toList();
        edges.stream().forEach(edge -> { injector.injectMembers(edge); edge.setGtfs(gtfs); });
        return edges;
    }

    @Override
    public Edge get(GtfsConfig gtfs, String id) {
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
        MongoDbEdge edge = query(gtfs).field("_id").equal(objectId).first();

        if (edge == null) {
            return null;
        }
        if (edge != null) {
            injector.injectMembers(edge);
            edge.setGtfs(gtfs);

        }
        return reverse ? new ReverseEdge(edge) : edge;
    }

    @Override
    public Edge create(GtfsConfig gtfs, Stop stop1, Stop stop2, Integer typicalTime) {
        return create(gtfs, stop1.getBaseId(), stop2.getBaseId(), typicalTime);
    }

    @Override
    public Edge create(GtfsConfig gtfs, String stop1Id, String stop2Id, Integer typicalTime) {
        MongoDbEdge edge = new MongoDbEdge(stopsModel, gtfs, stop1Id, stop2Id, typicalTime, true);
        gtfs.getDs().save(edge);
        edge.setGtfs(gtfs);
        return edge;
    }

    @Override
    public void update(GtfsConfig gtfs, Edge edge, Integer typicalTime) {
        MongoDbEdge mongoDbEdge = (MongoDbEdge)edge;
        mongoDbEdge.setTypicalTime(typicalTime);
        mongoDbEdge.setModified(true);
        gtfs.getDs().update(queryId(gtfs, mongoDbEdge.getObjectId()), ops(gtfs).set("typicalTime", mongoDbEdge.getTypicalTime()).set("modified", Boolean.TRUE));
    }

    @Override
    public void delete(GtfsConfig gtfs, Edge edge) {
        MongoDbEdge mongoDbEdge = (MongoDbEdge)edge;
        gtfs.getDs().delete(queryId(gtfs, mongoDbEdge.getObjectId()));
    }

    @Override
    public List<? extends Edge> getEdgesFrom(GtfsConfig gtfs, Stop stop) {
        String stopId = stop.getBaseId();

        Query<MongoDbEdge> query = query(gtfs);
        query.or(query.or(query.criteria("stop1Id").equal(stopId), query.criteria("stop2Id").equal(stopId)));
        List<MongoDbEdge> edges = query.asList();
        for(MongoDbEdge edge : edges) {
            injector.injectMembers(edge);
            edge.setGtfs(gtfs);
        }
        return edges;
    }

    @Override
    public List<? extends Edge> getByPoint(GtfsConfig gtfs, Point point) {
        Query<MongoDbEdge> query = query(gtfs);
        List<? extends Edge> edges = query.field("bbNorth").greaterThan(point.getLat()).field("bbSouth").lessThan(point.getLat()).field("bbEast").greaterThan(point.getLng()).field("bbWest").lessThan(point.getLng()).asList();
        edges.stream().forEach(e -> { injector.injectMembers(e); ((MongoDbEdge)e).setGtfs(gtfs); });

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
