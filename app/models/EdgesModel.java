package models;

import entities.Edge;
import java.util.List;

public interface EdgesModel {

    void drop();

    Edge save(Edge edge);

    List<? extends Edge> getAll();
}
