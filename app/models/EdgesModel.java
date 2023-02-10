package models;

import entities.Edge;
import entities.Stop;

import java.util.List;

public interface EdgesModel {

    void drop();

    Edge save(Edge edge);

    List<? extends Edge> getAll();

    Edge get(String id);

    Edge create(Stop stop1, Stop stop2, Integer typicalTime);

    void update(Edge edge, Integer typicalTime);

    void delete(Edge edge);

    List<? extends Edge> getEdgesFrom(Stop stop);
}
