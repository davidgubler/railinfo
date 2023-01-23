package biz;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.*;
import models.EdgesModel;
import models.RoutesModel;
import models.TripsModel;
import utils.NotAllowedException;
import utils.PathFinder;

public class Topology {
    @Inject
    private EdgesModel edgesModel;

    @Inject
    private RoutesModel routesModel;

    @Inject
    private TripsModel tripsModel;

    @Inject
    private Injector injector;

    @Inject
    private PathFinder pathFinder;

    public void edgeUpdate(Edge edge, int time, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        edgesModel.update(edge, time);

        // LOG
    }

    public void edgeDelete(Edge edge, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        edgesModel.delete(edge);

        // LOG
    }

    public void recalculateEdges(User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        new Thread(() -> {
            pathFinder.recalculateEdges();
        }).start();

        // LOG
    }

    public void recalculatePaths(User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        new Thread(() -> {
            pathFinder.recalculatePaths();
        }).start();

        // LOG
    }
}
