package biz;

import com.google.inject.Inject;
import com.google.inject.Injector;
import configs.GtfsConfig;
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

    public void edgeCreate(Http.RequestHeader request, GtfsConfig gtfs, Stop stop1, Stop stop2, Integer time, User user) throws InputValidationException {
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
        Edge edge = edgesModel.create(gtfs, stop1, stop2, time);
        pathFinder.clearCache(gtfs);

        // LOG
        RailinfoLogger.info(request, user + " created " + edge);
    }

    public void edgeUpdate(Http.RequestHeader request, GtfsConfig gtfs, Edge edge, Integer time, User user) throws InputValidationException {
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
        edgesModel.update(gtfs, edge, time);
        pathFinder.clearCache(gtfs);

        // LOG
        RailinfoLogger.info(request, user + " updated " + edge);
    }

    public void edgeDelete(Http.RequestHeader request, GtfsConfig gtfs, Edge edge, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        edgesModel.delete(gtfs, edge);
        pathFinder.clearCache(gtfs);

        // LOG
        RailinfoLogger.info(request, user + " deleted " + edge);
    }

    public void edgeDisable(Http.RequestHeader request, GtfsConfig gtfs, Edge edge, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        edgesModel.disable(gtfs, edge);
        pathFinder.clearCache(gtfs);

        // LOG
        RailinfoLogger.info(request, user + " disabled " + edge);
    }

    public void edgeEnable(Http.RequestHeader request, GtfsConfig gtfs, Edge edge, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        edgesModel.enable(gtfs, edge);
        pathFinder.clearCache(gtfs);

        // LOG
        RailinfoLogger.info(request, user + " enabled " + edge);
    }

    public void recalculateEdges(Http.RequestHeader request, GtfsConfig gtfs, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        new Thread(() -> {
            pathFinder.recalculateEdges(gtfs);
        }).start();

        // LOG
        RailinfoLogger.info(request, user + " recalculated edges");
    }

    public void recalculatePaths(Http.RequestHeader request, GtfsConfig gtfs, User user) {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT

        // BUSINESS
        new Thread(() -> {
            pathFinder.recalculatePaths(gtfs);
        }).start();

        // LOG
        RailinfoLogger.info(request, user + " recalculated paths");
    }

    public void stopCreate(Http.RequestHeader request, GtfsConfig gtfs, String name, Double lat, Double lng, User user) throws InputValidationException {
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
        Stop stop = stopsModel.create(gtfs, name, lat, lng);

        // LOG
        RailinfoLogger.info(request, user + " created " + stop);
    }

    public void stopUpdate(Http.RequestHeader request, GtfsConfig gtfs, Stop stop, String name, Double lat, Double lng, User user) throws InputValidationException {
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
        stopsModel.update(gtfs, stop, name, lat, lng);

        // LOG
        RailinfoLogger.info(request, user + " updated " + stop);
    }

    public void stopDelete(Http.RequestHeader request, GtfsConfig gtfs, Stop stop, User user) {
        // ACCESS
        if (user == null || !edgesModel.getEdgesFrom(gtfs, stop).isEmpty()) {
            throw new NotAllowedException();
        }

        // INPUT
        // nothing

        // BUSINESS
        stopsModel.delete(gtfs, stop);

        // LOG
        RailinfoLogger.info(request, user + " deleted " + stop);
    }
}
