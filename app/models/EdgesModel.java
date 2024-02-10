package models;

import configs.GtfsConfig;
import entities.Edge;
import entities.NearbyEdge;
import entities.Stop;
import geometry.Point;

import java.util.List;

public interface EdgesModel {
    void drop(GtfsConfig gtfs);

    Edge save(GtfsConfig v, Edge edge);

    List<? extends Edge> getAll(GtfsConfig gtfs, boolean includeDisabled);

    List<? extends Edge> getModified(GtfsConfig gtfs);

    Edge get(GtfsConfig gtfs, String id);

    Edge getByName(GtfsConfig gtfs, String name);

    Edge create(GtfsConfig gtfs, Stop stop1, Stop stop2, Integer typicalTime, boolean disabled);

    Edge create(GtfsConfig gtfs, String stop1Id, String stop2Id, Integer typicalTime, boolean disabled);

    void update(GtfsConfig gtfs, Edge edge, Integer typicalTime);

    void delete(GtfsConfig gtfs, Edge edge);

    void disable(GtfsConfig gtfs, Edge edge);

    void enable(GtfsConfig gtfs, Edge edge);

    List<? extends Edge> getEdgesFrom(GtfsConfig gtfs, Stop stop);

    Edge getEdgeBetween(GtfsConfig gtfs, Stop stop1, Stop stop2);

    List<NearbyEdge> getByPoint(GtfsConfig gtfs, Point point);
}
