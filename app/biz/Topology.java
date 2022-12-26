package biz;

import com.google.inject.Inject;
import entities.Edge;
import models.EdgesModel;

public class Topology {
    @Inject
    private EdgesModel edgesModel;

    public void updateEdge(Edge edge, int time) {
        edgesModel.update(edge, time);
    }
}
