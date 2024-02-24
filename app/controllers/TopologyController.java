package controllers;

import biz.Topology;
import com.google.inject.Inject;
import configs.GtfsConfig;
import entities.*;
import models.*;
import utils.*;
import play.mvc.Http;
import play.mvc.Result;

import java.util.*;

public class TopologyController extends GtfsController {

    @Inject
    private Topology topology;

    @Inject
    private EdgesModel edgesModel;

    @Inject
    private UsersModel usersModel;

    @Inject
    private GtfsConfigModel gtfsConfigModel;

    public Result edgesSearch(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);
        return ok(views.html.topology.edges.search.render(request, null, InputUtils.NOERROR, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result edgesSearchPost(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String partialName = InputUtils.trimToNull(data.get("partialName"));
        List<? extends Stop> stops = gtfs.getStopsModel().getByPartialName(gtfs, partialName);
        if (stops.isEmpty()) {
            return ok(views.html.topology.edges.search.render(request, partialName, Map.of("partialName", ErrorMessages.EDGE_NOT_FOUND), user, gtfsConfigModel.getSelectorChoices(), gtfs));
        }
        boolean edgesExist = false;
        for (Stop stop : stops) {
            if (!edgesModel.getEdgesFrom(gtfs, stop).isEmpty()) {
                edgesExist = true;
                break;
            }
        }
        if (!edgesExist) {
            return ok(views.html.topology.edges.search.render(request, partialName, Map.of("partialName", ErrorMessages.EDGE_NOT_FOUND), user, gtfsConfigModel.getSelectorChoices(), gtfs));
        }
        return redirect(controllers.routes.TopologyController.edgesList(gtfs.getCode(), partialName));
    }

    public Result edgesList(Http.Request request, String cc, String partialName) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);
        List<? extends Edge> edges = Collections.emptyList();
        if ("_".equals(partialName)) {
            return redirect(controllers.routes.TopologyController.edgesSearch(gtfs.getCode()));
        }
        if ("_modified".equals(partialName)) {
            edges = edgesModel.getModified(gtfs);
        } else {
            List<? extends Stop> stops = gtfs.getStopsModel().getByPartialName(gtfs, partialName);
            if (stops.isEmpty()) {
                throw new NotFoundException("Edge");
            }
            Set<Edge> edgesSet = new HashSet<>();
            stops.forEach(s -> edgesSet.addAll(edgesModel.getEdgesFrom(gtfs, s)));
            edges = new ArrayList<>(edgesSet);
        }
        Collections.sort(edges);
        return ok(views.html.topology.edges.list.render(request, partialName, edges, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result edgeCreate(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(user, gtfs);
        return ok(views.html.topology.edges.create.render(request, null, null, null, gtfs.getStopsModel().getAll(gtfs), InputUtils.NOERROR, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result edgeCreatePost(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(user, gtfs);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        Stop stop1 = gtfs.getStopsModel().getPrimaryByName(gtfs, InputUtils.trimToNull(data.get("stop1")));
        Stop stop2 = gtfs.getStopsModel().getPrimaryByName(gtfs, InputUtils.trimToNull(data.get("stop2")));
        Integer time = InputUtils.parseDuration(data.get("time"));
        try {
            topology.edgeCreate(request, gtfs, stop1, stop2, time, user);
        } catch (InputValidationException e) {
            return ok(views.html.topology.edges.create.render(request, stop1, stop2, time, gtfs.getStopsModel().getAll(gtfs), e.getErrors(), user, gtfsConfigModel.getSelectorChoices(), gtfs));
        }
        return redirect(controllers.routes.TopologyController.edgesList(gtfs.getCode(), "_modified"));
    }

    public Result edgeView(Http.Request request, String cc, String partialName, String edgeName) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Edge edge = edgesModel.getByName(gtfs, edgeName);
        check(gtfs, edge);
        return ok(views.html.topology.edges.view.render(request, partialName, edge, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result edgeEdit(Http.Request request, String cc, String partialName, String edgeName) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Edge edge = edgesModel.getByName(gtfs, edgeName);
        check(user, gtfs, edge);
        return ok(views.html.topology.edges.edit.render(request, partialName, edge, InputUtils.NOERROR, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result edgeEditPost(Http.Request request, String cc, String partialName, String edgeName) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Edge edge = edgesModel.getByName(gtfs, edgeName);
        check(user, gtfs, edge);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        Integer time = InputUtils.parseDuration(data.get("time"));
        try {
            topology.edgeUpdate(request, gtfs, edge, time, user);
        } catch (InputValidationException e) {
            return ok(views.html.topology.edges.edit.render(request, partialName, edge, e.getErrors(), user, gtfsConfigModel.getSelectorChoices(), gtfs));
        }
        return redirect(controllers.routes.TopologyController.edgesList(gtfs.getCode(), partialName));
    }

    public Result edgeDelete(Http.Request request, String cc, String partialName, String edgeName) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Edge edge = edgesModel.getByName(gtfs, edgeName);
        check(user, gtfs, edge);
        return ok(views.html.topology.edges.delete.render(request, partialName, edge, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result edgeDeletePost(Http.Request request, String cc, String partialName, String edgeName) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Edge edge = edgesModel.getByName(gtfs, edgeName);
        check(user, gtfs, edge);
        topology.edgeDelete(request, gtfs, edge, user);
        return redirect(controllers.routes.TopologyController.edgesList(gtfs.getCode(), partialName));
    }

    public Result edgeDisablePost(Http.Request request, String cc, String partialName, String edgeName) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Edge edge = edgesModel.getByName(gtfs, edgeName);
        check(user, gtfs, edge);
        topology.edgeDisable(request, gtfs, edge, user);
        return redirect(controllers.routes.TopologyController.edgesList(gtfs.getCode(), partialName));
    }

    public Result edgeEnablePost(Http.Request request, String cc, String partialName, String edgeName) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Edge edge = edgesModel.getByName(gtfs, edgeName);
        check(user, gtfs, edge);
        topology.edgeEnable(request, gtfs, edge, user);
        return redirect(controllers.routes.TopologyController.edgesList(gtfs.getCode(), partialName));
    }

    public Result recalculate(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(user, gtfs);
        return ok(views.html.topology.recalculate.render(request, user, gtfsConfigModel.getSelectorChoices(), gtfs));
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
        return ok(views.html.topology.map.render(request, edgesModel.getAll(gtfs, false), false, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result stopsSearch(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);
        return ok(views.html.topology.stops.search.render(request, null, InputUtils.NOERROR, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result stopsSearchPost(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String partialName = InputUtils.trimToNull(data.get("partialName"));
        List<? extends Stop> stops = gtfs.getStopsModel().getByPartialName(gtfs, partialName);
        if (stops.isEmpty()) {
            return ok(views.html.topology.stops.search.render(request, partialName, Map.of("partialName", ErrorMessages.STOP_NOT_FOUND), user, gtfsConfigModel.getSelectorChoices(), gtfs));
        }
        return redirect(controllers.routes.TopologyController.stopsList(gtfs.getCode(), partialName));
    }

    public Result stopsList(Http.Request request, String cc, String partialName) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);
        List<? extends Stop> stops = gtfs.getStopsModel().getByPartialName(gtfs, partialName);
        Collections.sort(stops);
        return ok(views.html.topology.stops.list.render(request, partialName, stops, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result stopsCreate(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(user, gtfs, true);
        return ok(views.html.topology.stops.edit.render(request, null, null, null, null, null, InputUtils.NOERROR, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result stopsCreatePost(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(user, gtfs, true);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String name = InputUtils.trimToNull(data.get("name"));
        String[] latlng = InputUtils.trimToNull(data.get("latlng")).split(",");
        Double lat = InputUtils.toDouble(latlng.length >= 2 ? latlng[0] : null);
        Double lng = InputUtils.toDouble(latlng.length >= 2 ? latlng[1] : null);
        try {
            topology.stopCreate(request, gtfs, name, lat, lng, user);
        } catch (InputValidationException e) {
            return ok(views.html.topology.stops.edit.render(request, null, null, name, lat, lng, e.getErrors(), user, gtfsConfigModel.getSelectorChoices(), gtfs));
        }
        return redirect(controllers.routes.TopologyController.stopsList(name, gtfs.getCode()));
    }

    public Result stopsEdit(Http.Request request, String cc, String partialName, String editStopId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Stop stop = gtfs.getStopsModel().get(gtfs, editStopId);
        check(user, gtfs, true, stop);
        return ok(views.html.topology.stops.edit.render(request, partialName, stop, stop.getName(), stop.getLat(), stop.getLng(), InputUtils.NOERROR, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result stopsEditPost(Http.Request request, String cc, String partialName, String editStopId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Stop stop = gtfs.getStopsModel().get(gtfs, editStopId);
        check(user, gtfs, true, stop);
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String name = InputUtils.trimToNull(data.get("name"));
        String[] latlng = InputUtils.trimToNull(data.get("latlng")).split(",");
        Double lat = InputUtils.toDouble(latlng.length >= 2 ? latlng[0] : null);
        Double lng = InputUtils.toDouble(latlng.length >= 2 ? latlng[1] : null);
        try {
            topology.stopUpdate(request, gtfs, stop, name, lat, lng, user);
        } catch (InputValidationException e) {
            return ok(views.html.topology.stops.edit.render(request, partialName, stop, name, lat, lng, e.getErrors(), user, gtfsConfigModel.getSelectorChoices(), gtfs));
        }
        return redirect(controllers.routes.TopologyController.stopsList(partialName, gtfs.getCode()));
    }

    public Result stopsDelete(Http.Request request, String cc, String partialName, String stopId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Stop stop = gtfs.getStopsModel().get(gtfs, stopId);
        check(user, gtfs, true, stop);
        if (!edgesModel.getEdgesFrom(gtfs, stop).isEmpty()) {
            throw new NotAllowedException();
        }
        return ok(views.html.topology.stops.delete.render(request, partialName, stop, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result stopsDeletePost(Http.Request request, String cc, String partialName, String stopId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        Stop stop = gtfs.getStopsModel().get(gtfs, stopId);
        check(user, gtfs, true, stop);
        topology.stopDelete(request, gtfs, stop, user);
        return redirect(controllers.routes.TopologyController.stopsList(partialName, gtfs.getCode()));
    }
}