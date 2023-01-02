package controllers;

import biz.Topology;
import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.*;
import entities.Edge;
import models.*;
import utils.InputUtils;
import utils.NotAllowedException;
import utils.NotFoundException;
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

    public Result edgesList(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        List<? extends Edge> edges = edgesModel.getAll();
        return ok(views.html.topology.edges.list.render(request, edges, user));
    }

    public Result edgeCreate(Http.Request request) {
        return ok();
    }

    public Result edgeCreatePost(Http.Request request) {
        return ok();
    }

    public Result edgeEdit(Http.Request request, String edgeId) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        Edge edge = edgesModel.get(edgeId);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }
        return ok(views.html.topology.edges.edit.render(request, edge, InputUtils.NOERROR, user));
    }

    public Result edgeEditPost(Http.Request request, String edgeId) {
        User user = usersModel.getFromRequest(request);
        Edge edge = edgesModel.get(edgeId);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        int typicalTime = InputUtils.parseDuration(data.get("typicalTime"));
        topology.edgeUpdate(edge, typicalTime, user);
        return redirect(controllers.routes.TopologyController.edgesList());
    }

    public Result edgeDelete(Http.Request request, String edgeId) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        Edge edge = edgesModel.get(edgeId);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }
        return ok(views.html.topology.edges.delete.render(request, edge, user));
    }

    public Result edgeDeletePost(Http.Request request, String edgeId) {
        User user = usersModel.getFromRequest(request);
        Edge edge = edgesModel.get(edgeId);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }
        topology.edgeDelete(edge, user);
        return redirect(controllers.routes.TopologyController.edgesList());
    }

    public Result recalculate(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        if (user == null) {
            throw new NotAllowedException();
        }
        return ok(views.html.topology.recalculate.render(request, user));
    }

    public Result recalculatePost(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        topology.recalculate(user);
        return redirect(controllers.routes.TopologyController.recalculate());
    }

    public Result map(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        return ok(views.html.topology.map.render(request, edgesModel.getAll(), user));
    }
}
