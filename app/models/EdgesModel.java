package models;


import entities.Edge;

public interface EdgesModel {

    void drop();

    Edge save(Edge edge);
}
