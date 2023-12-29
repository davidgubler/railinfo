package models;

import akka.japi.Pair;
import configs.GtfsConfig;
import entities.Edge;
import entities.NearbyEdge;
import entities.Stop;
import geometry.Point;

import java.util.List;

public interface EdgesModel {
    void drop(GtfsConfig gtfs);

    Edge save(GtfsConfig v, Edge edge);

    List<? extends Edge> getAll(GtfsConfig gtfs);

    List<? extends Edge> getModified(GtfsConfig gtfs);

    Edge get(GtfsConfig gtfs, String id);

    Edge create(GtfsConfig gtfs, Stop stop1, Stop stop2, Integer typicalTime);

    Edge create(GtfsConfig gtfs, String stop1Id, String stop2Id, Integer typicalTime);

    void update(GtfsConfig gtfs, Edge edge, Integer typicalTime);

    void delete(GtfsConfig gtfs, Edge edge);

    List<? extends Edge> getEdgesFrom(GtfsConfig gtfs, Stop stop);

    Edge getEdgeBetween(GtfsConfig gtfs, Stop stop1, Stop stop2);

    Edge getEdgeByString(GtfsConfig gtfs, String edgeName);

    List<NearbyEdge> getByPoint(GtfsConfig gtfs, Point point);
}
