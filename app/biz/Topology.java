package biz;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.*;
import models.EdgesModel;
import models.RoutesModel;
import models.StopsModel;
import models.TripsModel;
import play.mvc.Http;
import services.MongoDb;
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

    @Inject
    private MongoDb mongoDb;

    public void edgeCreate(Http.RequestHeader request, String databaseName, Stop stop1, Stop stop2, Integer time, User user) throws InputValidationException {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT
        Map<String, String> errors = new HashMap<>();
        InputUtils.validateObject(stop1, "stop1", true, errors);
        InputUtils.validateObject(stop2, "stop2", true, errors);
        InputUtils.validateInt(time, "time", true, 1, null, errors);
        if (!errors.isEmpty()) {
            throw new InputValidationException(errors);
        }

        // BUSINESS
        Edge edge = edgesModel.create(databaseName, stop1, stop2, time);
        pathFinder.clearCache();

        // LOG
        RailinfoLogger.info(request, user + " created " + edge);
    }

    public void edgeUpdate(Http.RequestHeader request, String databaseName, Edge edge, Integer time, User user) throws InputValidationException {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT
        Map<String, String> errors = new HashMap<>();
        InputUtils.validateInt(time, "time", true, 1, null, errors);
        if (!errors.isEmpty()) {
            throw new InputValidationException(errors);
        }

        // BUSINESS
        edgesModel.update(databaseName, edge, time);
        pathFinder.clearCache();

        // LOG
        RailinfoLogger.info(request, user + " updated " + edge);
    }

    public void edgeDelete(Http.RequestHeader request, String databaseName, Edge edge, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        edgesModel.delete(databaseName, edge);
        pathFinder.clearCache();

        // LOG
        RailinfoLogger.info(request, user + " deleted " + edge);
    }

    public void recalculateEdges(Http.RequestHeader request, String databaseName, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        new Thread(() -> {
            pathFinder.recalculateEdges(databaseName);
        }).start();

        // LOG
        RailinfoLogger.info(request, user + " recalculated edges");
    }

    public void recalculatePaths(Http.RequestHeader request, String databaseName, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        new Thread(() -> {
            pathFinder.recalculatePaths(databaseName);
        }).start();

        // LOG
        RailinfoLogger.info(request, user + " recalculated paths");
    }

    public void stopCreate(Http.RequestHeader request, String databaseName, String name, Double lat, Double lng, User user) throws InputValidationException {
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
        Stop stop = stopsModel.create(databaseName, name, lat, lng);

        // LOG
        RailinfoLogger.info(request, user + " created " + stop);
    }

    public void stopUpdate(Http.RequestHeader request, String databaseName, Stop stop, String name, Double lat, Double lng, User user) throws InputValidationException {
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
        stopsModel.update(databaseName, stop, name, lat, lng);

        // LOG
        RailinfoLogger.info(request, user + " updated " + stop);
    }

    public void stopDelete(Http.RequestHeader request, String databaseName, Stop stop, User user) {
        // ACCESS
        if (user == null || !edgesModel.getEdgesFrom(databaseName, stop).isEmpty()) {
            throw new NotAllowedException();
        }

        // INPUT
        // nothing

        // BUSINESS
        stopsModel.delete(databaseName, stop);

        // LOG
        RailinfoLogger.info(request, user + " deleted " + stop);
    }
}
