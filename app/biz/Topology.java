package biz;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.*;
import models.EdgesModel;
import models.RoutesModel;
import models.StopsModel;
import models.TripsModel;
import play.mvc.Http;
import utils.*;

import java.util.HashMap;
import java.util.Map;

public class Topology {
    @Inject
    private EdgesModel edgesModel;

    @Inject
    private StopsModel stopsModel;

    @Inject
    private RoutesModel routesModel;

    @Inject
    private TripsModel tripsModel;

    @Inject
    private Injector injector;

    @Inject
    private PathFinder pathFinder;

    public void edgeUpdate(Http.RequestHeader request, Edge edge, int time, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        edgesModel.update(edge, time);

        // LOG
        RailinfoLogger.info(request, user + " updated " + edge);
    }

    public void edgeDelete(Http.RequestHeader request, Edge edge, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        edgesModel.delete(edge);

        // LOG
        RailinfoLogger.info(request, user + " deleted " + edge);
    }

    public void recalculateEdges(Http.RequestHeader request, User user) {
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
        RailinfoLogger.info(request, user + " recalculated edges");
    }

    public void recalculatePaths(Http.RequestHeader request, User user) {
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
        RailinfoLogger.info(request, user + " recalculated paths");
    }

    public void stopCreate(Http.RequestHeader request, String name, Double lat, Double lng, User user) throws InputValidationException {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT
        Map<String, String> errors = new HashMap<>();
        InputUtils.validateString(name, "name", errors);
        if (!errors.isEmpty()) {
            throw new InputValidationException(errors);
        }

        // BUSINESS
        Stop stop = stopsModel.create(name, lat, lng);

        // LOG
        RailinfoLogger.info(request, user + " created " + stop);
    }

    public void stopUpdate(Http.RequestHeader request, Stop stop, String name, Double lat, Double lng, User user) throws InputValidationException {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT
        Map<String, String> errors = new HashMap<>();
        InputUtils.validateString(name, "name", errors);
        if (!errors.isEmpty()) {
            throw new InputValidationException(errors);
        }

        // BUSINESS
        //edgesModel.update(edge, time);

        // LOG
        RailinfoLogger.info(request, user + " updated " + stop);
    }

    public void stopDelete(Http.RequestHeader request, Stop stop, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        //edgesModel.delete(edge);

        // LOG
        RailinfoLogger.info(request, user + " deleted " + stop);
    }
}
