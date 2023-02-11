package models;

import entities.Edge;
import entities.Stop;

import java.util.List;

public interface EdgesModel {

    void drop(String databaseName);

    Edge save(String databaseName, Edge edge);

    List<? extends Edge> getAll(String databaseName);

    List<? extends Edge> getModified(String databaseName);

    Edge get(String databaseName, String id);

    Edge create(String databaseName, Stop stop1, Stop stop2, Integer typicalTime);

    void update(String databaseName, Edge edge, Integer typicalTime);

    void delete(String databaseName, Edge edge);

    List<? extends Edge> getEdgesFrom(String databaseName, Stop stop);
}
