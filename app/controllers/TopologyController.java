package controllers;

import biz.Topology;
import com.google.inject.Inject;
import com.google.inject.Injector;
import configs.GtfsConfig;
import entities.*;
import models.*;
import services.MongoDb;
import utils.*;
import play.mvc.Http;
import play.mvc.Result;

import java.util.*;

public class TopologyController extends GtfsController {

    @Inject
    private Topology topology;

    @Inject
    private StopsModel stopsModel;

    @Inject
    private StopTimesModel stopTimesModel;

    @Inject
    private TripsModel tripsModel;

    @Inject
    private ServiceCalendarsModel serviceCalendarsModel;

    @Inject
    private ServiceCalendarExceptionsModel serviceCalendarExceptionsModel;

    @Inject
    private RoutesModel routesModel;

    @Inject
    private EdgesModel edgesModel;

    @Inject
    private UsersModel usersModel;

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    @Inject
    private GtfsConfigModel gtfsConfigModel;



    public Result edgesList(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);
        List<? extends Edge> edges = edgesModel.getAll(gtfs);
        return ok(views.html.topology.edges.list.render(request, edges, user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
    }

    public Result edgesCreate(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(user, gtfs);
        return ok(views.html.topology.edges.create.render(request, null, null, null, stopsModel.getAll(gtfs), InputUtils.NOERROR, user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
    }

    public Result edgesCreatePost(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(user, gtfs);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        Stop stop1 = stopsModel.getPrimaryByName(gtfs, InputUtils.trimToNull(data.get("stop1")));
        Stop stop2 = stopsModel.getPrimaryByName(gtfs, InputUtils.trimToNull(data.get("stop2")));
        Integer time = InputUtils.parseDuration(data.get("time"));
        try {
            topology.edgeCreate(request, gtfs, stop1, stop2, time, user);
        } catch (InputValidationException e) {
            return ok(views.html.topology.edges.create.render(request, stop1, stop2, time, stopsModel.getAll(gtfs), e.getErrors(), user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
        }
        return redirect(controllers.routes.TopologyController.edgesList(gtfs.getCode()));
    }

    public Result edgesView(Http.Request request, String cc, String edgeId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Edge edge = edgesModel.get(gtfs, edgeId);
        check(gtfs, edge);
        return ok(views.html.topology.edges.view.render(request, edge, user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
    }

    public Result edgesEdit(Http.Request request, String cc, String edgeId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Edge edge = edgesModel.get(gtfs, edgeId);
        check(user, gtfs, edge);
        return ok(views.html.topology.edges.edit.render(request, edge, InputUtils.NOERROR, user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
    }

    public Result edgesEditPost(Http.Request request, String cc, String edgeId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Edge edge = edgesModel.get(gtfs, edgeId);
        check(user, gtfs, edge);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        Integer time = InputUtils.parseDuration(data.get("time"));
        try {
            topology.edgeUpdate(request, gtfs, edge, time, user);
        } catch (InputValidationException e) {
            return ok(views.html.topology.edges.edit.render(request, edge, e.getErrors(), user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
        }
        return redirect(controllers.routes.TopologyController.edgesList(gtfs.getCode()));
    }

    public Result edgesDelete(Http.Request request, String cc, String edgeId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Edge edge = edgesModel.get(gtfs, edgeId);
        check(user, gtfs, edge);
        return ok(views.html.topology.edges.delete.render(request, edge, user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
    }

    public Result edgesDeletePost(Http.Request request, String cc, String edgeId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Edge edge = edgesModel.get(gtfs, edgeId);
        check(user, gtfs, edge);
        topology.edgeDelete(request, gtfs, edge, user);
        return redirect(controllers.routes.TopologyController.edgesList(gtfs.getCode()));
    }

    public Result recalculate(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(user, gtfs);
        return ok(views.html.topology.recalculate.render(request, user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
    }

    public Result recalculateEdgesPost(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(user, gtfs);
        topology.recalculateEdges(request, gtfs, user);
        return redirect(controllers.routes.TopologyController.recalculate(gtfs.getCode()));
    }

    public Result recalculatePathsPost(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(user, gtfs);
        topology.recalculatePaths(request, gtfs, user);
        return redirect(controllers.routes.TopologyController.recalculate(gtfs.getCode()));
    }

    public Result map(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);
        return ok(views.html.topology.map.render(request, edgesModel.getAll(gtfs), user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
    }

    public Result stopsSearch(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);
        return ok(views.html.topology.stops.search.render(request, null, InputUtils.NOERROR, user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
    }

    public Result stopsSearchPost(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String partialName = InputUtils.trimToNull(data.get("partialName"));
        List<? extends Stop> stops = stopsModel.getByPartialName(gtfs, partialName);
        if (stops.isEmpty()) {
            return ok(views.html.topology.stops.search.render(request, partialName, Map.of("partialName", ErrorMessages.STOP_NOT_FOUND), user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
        }
        return redirect(controllers.routes.TopologyController.stopsList(gtfs.getCode(), partialName));
    }

    public Result stopsList(Http.Request request, String cc, String partialName) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);
        List<? extends Stop> stops = stopsModel.getByPartialName(gtfs, partialName);
        Collections.sort(stops);
        return ok(views.html.topology.stops.list.render(request, partialName, stops, user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
    }

    public Result stopsCreate(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(user, gtfs);
        return ok(views.html.topology.stops.edit.render(request, null, null, null, null, null, InputUtils.NOERROR, user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
    }

    public Result stopsCreatePost(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(user, gtfs);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String name = InputUtils.trimToNull(data.get("name"));
        String[] latlng = InputUtils.trimToNull(data.get("latlng")).split(",");
        Double lat = InputUtils.toDouble(latlng.length >= 2 ? latlng[0] : null);
        Double lng = InputUtils.toDouble(latlng.length >= 2 ? latlng[1] : null);
        try {
            topology.stopCreate(request, gtfs, name, lat, lng, user);
        } catch (InputValidationException e) {
            return ok(views.html.topology.stops.edit.render(request, null, null, name, lat, lng, e.getErrors(), user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
        }
        return redirect(controllers.routes.TopologyController.stopsList(name, gtfs.getCode()));
    }

    public Result stopsEdit(Http.Request request, String cc, String partialName, String editStopId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Stop stop = stopsModel.get(gtfs, editStopId);
        check(user, gtfs, stop);
        return ok(views.html.topology.stops.edit.render(request, partialName, stop, stop.getName(), stop.getLat(), stop.getLng(), InputUtils.NOERROR, user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
    }

    public Result stopsEditPost(Http.Request request, String cc, String partialName, String editStopId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Stop stop = stopsModel.get(gtfs, editStopId);
        check(user, gtfs, stop);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String name = InputUtils.trimToNull(data.get("name"));
        String[] latlng = InputUtils.trimToNull(data.get("latlng")).split(",");
        Double lat = InputUtils.toDouble(latlng.length >= 2 ? latlng[0] : null);
        Double lng = InputUtils.toDouble(latlng.length >= 2 ? latlng[1] : null);
        try {
            topology.stopUpdate(request, gtfs, stop, name, lat, lng, user);
        } catch (InputValidationException e) {
            return ok(views.html.topology.stops.edit.render(request, partialName, stop, name, lat, lng, e.getErrors(), user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
        }
        return redirect(controllers.routes.TopologyController.stopsList(partialName, gtfs.getCode()));
    }

    public Result stopsDelete(Http.Request request, String cc, String partialName, String stopId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Stop stop = stopsModel.get(gtfs, stopId);
        check(user, gtfs, stop);
        if (!edgesModel.getEdgesFrom(gtfs, stop).isEmpty()) {
            throw new NotAllowedException();
        }
        return ok(views.html.topology.stops.delete.render(request, partialName, stop, user, gtfsConfigModel.getSelectorChoices(), gtfs.getCode()));
    }

    public Result stopsDeletePost(Http.Request request, String cc, String partialName, String stopId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Stop stop = stopsModel.get(gtfs, stopId);
        check(user, gtfs, stop);
        topology.stopDelete(request, gtfs, stop, user);
        return redirect(controllers.routes.TopologyController.stopsList(partialName, gtfs.getCode()));
    }
}