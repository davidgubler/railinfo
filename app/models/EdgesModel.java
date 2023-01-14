package models;

import entities.Edge;
import entities.Stop;

import java.util.List;

public interface EdgesModel {

    void drop();

    Edge save(Edge edge);

    List<? extends Edge> getAll();

    Edge get(String id);

    void update(Edge edge, int typicalTime);

    void delete(Edge edge);

    List<? extends Edge> getEdgesFrom(Stop stop);
}
