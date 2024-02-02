package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.morphia.UpdateOptions;
import dev.morphia.query.filters.Filters;
import configs.GtfsConfig;
import dev.morphia.query.Query;
import dev.morphia.query.updates.UpdateOperators;
import entities.Edge;
import entities.NearbyEdge;
import entities.ReverseEdge;
import entities.Stop;
import entities.mongodb.MongoDbEdge;
import geometry.Point;
import models.EdgesModel;
import models.StopsModel;
import org.bson.types.ObjectId;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MongoDbEdgesModel implements EdgesModel {
    @Inject
    private Injector injector;

    @Inject
    private StopsModel stopsModel;

    private Query<MongoDbEdge> query(GtfsConfig gtfs) {
        return gtfs.getDs().find(MongoDbEdge.class);
    }

    private Query<MongoDbEdge> queryId(GtfsConfig gtfs, ObjectId objectId) {
        return query(gtfs).filter(Filters.eq("_id", objectId));
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
        List<? extends MongoDbEdge> edges = query(gtfs).iterator().toList();
        edges.stream().forEach(edge -> { injector.injectMembers(edge); edge.setGtfs(gtfs); });
        return edges;
    }

    @Override
    public List<? extends Edge> getModified(GtfsConfig gtfs) {
        List<? extends MongoDbEdge> edges = query(gtfs).filter(Filters.eq("modified", true)).iterator().toList();
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
        MongoDbEdge edge = queryId(gtfs, objectId).first();

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
    public Edge getByName(GtfsConfig gtfs, String name) {
        String[] stopNames = name.split("-");
        if (stopNames.length != 2) {
            return null;
        }
        Stop stop1 = stopsModel.getPrimaryByName(gtfs, stopNames[0]);
        if (stop1 == null) {
            return null;
        }
        Stop stop2 = stopsModel.getPrimaryByName(gtfs, stopNames[1]);
        if (stop2 == null) {
            return null;
        }
        Edge edge = getEdgeBetween(gtfs, stop1, stop2);
        if (edge == null) {
            return null;
        }

        if (edge.getStop1().equals(stop1)) {
            return edge;
        } else {
            return new ReverseEdge(edge);
        }
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
        queryId(gtfs, mongoDbEdge.getObjectId()).update(new UpdateOptions(), UpdateOperators.set("typicalTime", mongoDbEdge.getTypicalTime()), UpdateOperators.set("modified", Boolean.TRUE));
    }

    @Override
    public void delete(GtfsConfig gtfs, Edge edge) {
        MongoDbEdge mongoDbEdge = (MongoDbEdge)edge;
        queryId(gtfs, mongoDbEdge.getObjectId()).delete();
    }

    @Override
    public List<? extends Edge> getEdgesFrom(GtfsConfig gtfs, Stop stop) {
        String stopId = stop.getBaseId();
        List<MongoDbEdge> edges = query(gtfs).filter(Filters.or(Filters.eq("stop1Id", stopId), Filters.eq("stop2Id", stopId))).iterator().toList();
        for(MongoDbEdge edge : edges) {
            injector.injectMembers(edge);
            edge.setGtfs(gtfs);
        }
        return edges;
    }

    @Override
    public List<NearbyEdge> getByPoint(GtfsConfig gtfs, Point point) {
        List<? extends Edge> edges = query(gtfs).filter(
                Filters.gt("bbNorth", point.getLat()),
                Filters.lt("bbSouth", point.getLat()),
                Filters.gt("bbEast", point.getLng()),
                Filters.lt("bbWest", point.getLng())
        ).iterator().toList();
        edges.stream().forEach(e -> { injector.injectMembers(e); ((MongoDbEdge)e).setGtfs(gtfs); });

        List<NearbyEdge> nearbyEdges = new LinkedList<>();
        for (Edge edge : edges) {
            nearbyEdges.add(new NearbyEdge(point, edge));
        }
        NearbyEdge.normalizeNearbyFactors(nearbyEdges);
        nearbyEdges = nearbyEdges.stream().filter(ne -> ne.getNearbyFactor() >= 0.01).collect(Collectors.toList());
        Collections.sort(nearbyEdges);
        Collections.reverse(nearbyEdges);
        return nearbyEdges;
    }

    @Override
    public Edge getEdgeBetween(GtfsConfig gtfs, Stop stop1, Stop stop2) {
        String stop1Id = stop1.getBaseId();
        String stop2Id = stop2.getBaseId();
        MongoDbEdge edge = query(gtfs).filter(Filters.or(
                Filters.and(Filters.eq("stop1Id", stop1Id), Filters.eq("stop2Id", stop2Id)),
                Filters.and(Filters.eq("stop1Id", stop2Id), Filters.eq("stop2Id", stop1Id))
        )).first();
        if (edge == null) {
            return null;
        }
        injector.injectMembers(edge);
        edge.setGtfs(gtfs);
        return edge;
    }
}
