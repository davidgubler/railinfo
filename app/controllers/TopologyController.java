package controllers;

import biz.Topology;
import com.google.inject.Inject;
import com.google.inject.Injector;
import configs.GtfsConfig;
import entities.*;
import models.*;
import services.MongoDb;
import utils.*;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.*;

public class TopologyController extends Controller {

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

    public Result edgesList(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        List<? extends Edge> edges = edgesModel.getAll(gtfs);
        return ok(views.html.topology.edges.list.render(request, edges, user));
    }

    public Result edgesCreate(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        return ok(views.html.topology.edges.create.render(request, null, null, null, stopsModel.getAll(gtfs), InputUtils.NOERROR, user));
    }

    public Result edgesCreatePost(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        Stop stop1 = stopsModel.getPrimaryByName(gtfs, InputUtils.trimToNull(data.get("stop1")));
        Stop stop2 = stopsModel.getPrimaryByName(gtfs, InputUtils.trimToNull(data.get("stop2")));
        Integer time = InputUtils.parseDuration(data.get("time"));
        try {
            topology.edgeCreate(request, gtfs, stop1, stop2, time, user);
        } catch (InputValidationException e) {
            return ok(views.html.topology.edges.create.render(request, stop1, stop2, time, stopsModel.getAll(gtfs), e.getErrors(), user));
        }
        return redirect(controllers.routes.TopologyController.edgesList());
    }

    public Result edgesView(Http.Request request, String edgeId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Edge edge = edgesModel.get(gtfs, edgeId);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }
        return ok(views.html.topology.edges.view.render(request, edge, user));
    }

    public Result edgesEdit(Http.Request request, String edgeId) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Edge edge = edgesModel.get(gtfs, edgeId);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }
        return ok(views.html.topology.edges.edit.render(request, edge, InputUtils.NOERROR, user));
    }

    public Result edgesEditPost(Http.Request request, String edgeId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Edge edge = edgesModel.get(gtfs, edgeId);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        Integer time = InputUtils.parseDuration(data.get("time"));
        try {
            topology.edgeUpdate(request, gtfs, edge, time, user);
        } catch (InputValidationException e) {
            return ok(views.html.topology.edges.edit.render(request, edge, e.getErrors(), user));
        }
        return redirect(controllers.routes.TopologyController.edgesList());
    }

    public Result edgesDelete(Http.Request request, String edgeId) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Edge edge = edgesModel.get(gtfs, edgeId);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }
        return ok(views.html.topology.edges.delete.render(request, edge, user));
    }

    public Result edgesDeletePost(Http.Request request, String edgeId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Edge edge = edgesModel.get(gtfs, edgeId);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }
        topology.edgeDelete(request, gtfs, edge, user);
        return redirect(controllers.routes.TopologyController.edgesList());
    }

    public Result recalculate(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        return ok(views.html.topology.recalculate.render(request, user));
    }

    public Result recalculateEdgesPost(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        topology.recalculateEdges(request, gtfs, user);
        return redirect(controllers.routes.TopologyController.recalculate());
    }

    public Result recalculatePathsPost(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        topology.recalculatePaths(request, gtfs, user);
        return redirect(controllers.routes.TopologyController.recalculate());
    }

    public Result map(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        return ok(views.html.topology.map.render(request, edgesModel.getAll(gtfs), user));
    }

    public Result stopsSearch(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        return ok(views.html.topology.stops.search.render(request, null, InputUtils.NOERROR, user));
    }

    public Result stopsSearchPost(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String partialName = InputUtils.trimToNull(data.get("partialName"));
        List<? extends Stop> stops = stopsModel.getByPartialName(gtfs, partialName);
        if (stops.isEmpty()) {
            return ok(views.html.topology.stops.search.render(request, partialName, Map.of("partialName", ErrorMessages.STOP_NOT_FOUND), user));
        }
        return redirect(controllers.routes.TopologyController.stopsList(partialName));
    }

    public Result stopsList(Http.Request request, String partialName) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        List<? extends Stop> stops = stopsModel.getByPartialName(gtfs, partialName);
        Collections.sort(stops);
        return ok(views.html.topology.stops.list.render(request, partialName, stops, user));
    }

    public Result stopsCreate(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        return ok(views.html.topology.stops.edit.render(request, null, null, null, null, null, InputUtils.NOERROR, user));
    }

    public Result stopsCreatePost(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String name = InputUtils.trimToNull(data.get("name"));
        String[] latlng = InputUtils.trimToNull(data.get("latlng")).split(",");
        Double lat = InputUtils.toDouble(latlng.length >= 2 ? latlng[0] : null);
        Double lng = InputUtils.toDouble(latlng.length >= 2 ? latlng[1] : null);
        try {
            topology.stopCreate(request, gtfs, name, lat, lng, user);
        } catch (InputValidationException e) {
            return ok(views.html.topology.stops.edit.render(request, null, null, name, lat, lng, e.getErrors(), user));
        }
        return redirect(controllers.routes.TopologyController.stopsList(name));
    }

    public Result stopsEdit(Http.Request request, String partialName, String editStopId) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Stop editStop = stopsModel.get(gtfs, editStopId);
        if (editStop == null) {
            throw new NotFoundException("Stop");
        }
        return ok(views.html.topology.stops.edit.render(request, partialName, editStop, editStop.getName(), editStop.getLat(), editStop.getLng(), InputUtils.NOERROR, user));
    }

    public Result stopsEditPost(Http.Request request, String partialName, String editStopId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Stop editStop = stopsModel.get(gtfs, editStopId);
        if (editStop == null) {
            throw new NotFoundException("Stop");
        }
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String name = InputUtils.trimToNull(data.get("name"));
        String[] latlng = InputUtils.trimToNull(data.get("latlng")).split(",");
        Double lat = InputUtils.toDouble(latlng.length >= 2 ? latlng[0] : null);
        Double lng = InputUtils.toDouble(latlng.length >= 2 ? latlng[1] : null);
        try {
            topology.stopUpdate(request, gtfs, editStop, name, lat, lng, user);
        } catch (InputValidationException e) {
            return ok(views.html.topology.stops.edit.render(request, partialName, editStop, name, lat, lng, e.getErrors(), user));
        }
        return redirect(controllers.routes.TopologyController.stopsList(partialName));
    }

    public Result stopsDelete(Http.Request request, String partialName, String stopId) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Stop stop = stopsModel.get(gtfs, stopId);
        if (stop == null) {
            throw new NotFoundException("Stop");
        }
        if (!edgesModel.getEdgesFrom(gtfs, stop).isEmpty()) {
            throw new NotAllowedException();
        }
        return ok(views.html.topology.stops.delete.render(request, partialName, stop, user));
    }

    public Result stopsDeletePost(Http.Request request, String partialName, String stopId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Stop stop = stopsModel.get(gtfs, stopId);
        if (stop == null) {
            throw new NotFoundException("Stop");
        }
        topology.stopDelete(request, gtfs, stop, user);
        return redirect(controllers.routes.TopologyController.stopsList(partialName));
    }
}