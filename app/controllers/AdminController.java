package controllers;

import biz.Users;
import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.User;
import entities.mongodb.MongoDbEdge;
import entities.Route;
import entities.StopTime;
import entities.Trip;
import models.*;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.InputUtils;
import utils.InputValidationException;
import utils.NotFoundException;

import java.util.*;

public class AdminController extends Controller {
    @Inject
    private RoutesModel routesModel;

    @Inject
    private TripsModel tripsModel;

    @Inject
    private StopsModel stopsModel;

    @Inject
    private EdgesModel edgesModel;

    @Inject
    private UsersModel usersModel;

    @Inject
    private Users users;

    @Inject
    private Injector injector;

    public Result usersList(Http.Request request) {
        List<? extends User> users = usersModel.getAll();
        return ok(views.html.admin.users.list.render(request, users));
    }

    public Result usersCreate(Http.Request request) {
        return ok(views.html.admin.users.create.render(request, null, null, null, InputUtils.NOERROR));
    }

    public Result usersCreatePost(Http.Request request) {
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String email = InputUtils.trimToNull(data.get("email"));
        String name = InputUtils.trimToNull(data.get("name"));
        String password = InputUtils.trimToNull(data.get("password"));
        try {
            users.create(email, name, password, null);
            return redirect(controllers.routes.AdminController.usersList());
        } catch (InputValidationException e) {
            return ok(views.html.admin.users.create.render(request, email, name, password, e.getErrors()));
        }
    }

    public Result usersEdit(Http.Request request, String uid) {
        User editUser = usersModel.get(uid);
        if (editUser == null) {
            throw new NotFoundException("User");
        }
        return ok(views.html.admin.users.edit.render(request, editUser, editUser.getEmail(), editUser.getName(), null, InputUtils.NOERROR));
    }

    public Result usersEditPost(Http.Request request, String uid) {
        Map<String, String[]> data = request.body().asFormUrlEncoded();
        User editUser = usersModel.get(uid);
        String email = InputUtils.trimToNull(data.get("email"));
        String name = InputUtils.trimToNull(data.get("name"));
        String password = InputUtils.trimToNull(data.get("password"));
        try {
            users.update(editUser, email, name, password, null);
            return redirect(controllers.routes.AdminController.usersList());
        } catch (InputValidationException e) {
            return ok(views.html.admin.users.edit.render(request, editUser, email, name, password, e.getErrors()));
        }
    }

    public Result usersDelete(Http.Request request, String uid) {
        User deleteUser = usersModel.get(uid);
        if (deleteUser == null) {
            throw new NotFoundException("User");
        }
        return ok(views.html.admin.users.delete.render(request, deleteUser));
    }

    public Result usersDeletePost(Http.Request request, String uid) {
        User deleteUser = usersModel.get(uid);
        if (deleteUser == null) {
            throw new NotFoundException("User");
        }
        users.delete(deleteUser, null);
        return redirect(controllers.routes.AdminController.usersList());
    }

    @AddCSRFToken
    public Result admin(Http.Request request) {
        return ok(views.html.admin.index.render(request));
    }

    @RequireCSRFCheck
    public Result recalculateEdgesPost(Http.Request request) {
        new Thread(() -> {

            Map<String, List<MongoDbEdge>> edgesFromStop = new HashMap<>();


            long start;

            start = System.currentTimeMillis();
            System.out.print("fetching rail routes... ");
            List<Route> railRoutes = routesModel.getByType(100, 199);
            System.out.println(railRoutes.size() + " in " + (System.currentTimeMillis() - start) + " ms");


            start = System.currentTimeMillis();
            System.out.print("extracting edges... ");
            for (Route route : railRoutes) {
                System.out.println("Route: " + route.getShortName());
                List<Trip> trips = tripsModel.getByRoute(route);
                for (Trip trip : trips) {
                    String lastStopId = null;
                    int depHour = 0, depMinute = 0, depSecond = 0;
                    List<StopTime> stopTimes = trip.getStopTimes();

                    for (StopTime stopTime : stopTimes) {
                        if (lastStopId != null) {
                            String[] split = stopTime.getArrival().split(":");
                            int arrHour = Integer.parseInt(split[0]);
                            int arrMinute = Integer.parseInt(split[1]);
                            int arrSecond = Integer.parseInt(split[2]);
                            int seconds = ((arrHour - depHour) * 60 + arrMinute - depMinute) * 60 + arrSecond - depSecond;


                            if (!edgesFromStop.containsKey(lastStopId)) {
                                edgesFromStop.put(lastStopId, new LinkedList<>());
                            }
                            MongoDbEdge edge = edgesFromStop.get(lastStopId).stream().filter(e -> e.getStop2Id().equals(stopTime.getParentStopId())).findFirst().orElse(null);
                            if (edge == null) {
                                edge = new MongoDbEdge(lastStopId, stopTime.getParentStopId());
                                injector.injectMembers(edge);
                                edgesFromStop.get(lastStopId).add(edge);
                            }
                            edge.addJourney(seconds);

                        }
                        lastStopId = stopTime.getParentStopId();
                        String[] split = stopTime.getDeparture().split(":");
                        depHour = Integer.parseInt(split[0]);
                        depMinute = Integer.parseInt(split[1]);
                        depSecond = Integer.parseInt(split[2]);
                    }
                }
            }
            List<MongoDbEdge> allEdges = new LinkedList<>();
            for (Map.Entry<String, List<MongoDbEdge>> entry : edgesFromStop.entrySet()) {
                allEdges.addAll(entry.getValue());
            }
            System.out.println(allEdges.size() + " in " + (System.currentTimeMillis() - start) + " ms");

            allEdges.sort(Comparator.comparing(MongoDbEdge::getTypicalTime));

            edgesModel.drop();
            for (MongoDbEdge edge : allEdges) {
                edgesModel.save(edge);
                //System.out.println(stopsModel.getById(e.getFromStopId()) + " -> " + stopsModel.getById(e.getToStopId()) + ": " + e.getTypicalTime() + "s");
            }

        }).run();

        return ok();
    }
}
