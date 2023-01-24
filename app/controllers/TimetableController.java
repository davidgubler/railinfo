package controllers;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.*;
import entities.realized.*;
import models.*;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.NotFoundException;
import utils.PathFinder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TimetableController extends Controller {

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
    private EdgesModel edgesModel;

    @Inject
    private UsersModel usersModel;

    @Inject
    private PathFinder pathFinder;

    @Inject
    private RoutesModel routesModel;

    @Inject
    private Injector injector;

    public Result index(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        return ok(views.html.timetable.index.render(request, user));
    }

    public Result departures(Http.Request request, String stopStr)  {
        User user = usersModel.getFromRequest(request);
        LocalDateTime dateTime = LocalDateTime.now();

        Set<Stop> stops = stopsModel.getByName(stopStr);
        List<StopTime> stopTimes = stopTimesModel.getByStops(stops);
        List<Trip> trips = new LinkedList<>();
        for (StopTime stopTime : stopTimes) {
            Trip trip = tripsModel.getByTripId(stopTime.getTripId());
            if (trip != null) {
                trips.add(trip);
            }
        }

        List<RealizedTrip> realizedTrips = new LinkedList<>();
        // we realize trips for yesterday, today and tomorrow in order to be sure to cover the time window from now to +12h
        // note that this will not be sufficient for countries that have trains running for longer than 1 day
        for (int i = -1; i <= 1; i++) {
            LocalDate d = dateTime.toLocalDate().plusDays(i);
            realizedTrips.addAll(trips.stream().filter(t -> t.isActive(d)).map(t -> {
                RealizedTrip realizedTrip = new RealizedTrip(t, d);
                injector.injectMembers(realizedTrip);
                return realizedTrip;
            }).collect(Collectors.toSet()));
        }
        System.out.println("realized trips: " + realizedTrips.size());

        List<RealizedDeparture> departures = new LinkedList<>();
        for (RealizedTrip realizedTrip : realizedTrips) {
            RealizedDeparture departure = realizedTrip.getDeparture(stops);
            if (departure.getDepartureTime() == null) {
                // train ends here
                continue;
            }
            if (departure.getDepartureTime().isBefore(dateTime) || departure.getDepartureTime().isAfter(dateTime.plusHours(12)) ) {
                // departure is in the past or more than 12h in the future
                continue;
            }
            if (departure.getStopTimes().size() <= 1) {
                // train ends here
                continue;
            }
            departures.add(departure);
        }
        Collections.sort(departures);

        return ok(views.html.timetable.stop.render(request, departures, user));
    }

    public Result movements(Http.Request request, String stopStr)  {
        User user = usersModel.getFromRequest(request);
        LocalDateTime dateTime = LocalDateTime.now();
        Set<Stop> stops = stopsModel.getByName(stopStr);
        return ok();
    }


    public Result realizedTrip(Http.Request request, String tripId, String startDateStr)  {
        User user = usersModel.getFromRequest(request);
        LocalDate startDate = LocalDate.parse(startDateStr);
        RealizedTrip realizedTrip = tripsModel.getRealizedTrip(tripId, startDate);
        if (realizedTrip == null) {
            throw new NotFoundException("Realized Trip");
        }
        return ok(views.html.timetable.realizedTrip.render(request, realizedTrip, user));
    }

    public Result edge(Http.Request request, String edgeId) {
        User user = usersModel.getFromRequest(request);
        Edge edge = edgesModel.get(edgeId);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }

        Set<String> routeIds = pathFinder.getRouteIdsByEdge(edge);
        Set<Route> routes = new HashSet<>();
        for (String routeId : routeIds) {
            routes.add(routesModel.getByRouteId(routeId));
        }

        Set<Trip> trips = new HashSet<>();
        for (Route route : routes) {
            trips.addAll(tripsModel.getByRoute(route));
        }

        List<RealizedTrip> realizedTrips = new LinkedList<>();
        LocalDateTime dateTime = LocalDateTime.now();
        for (int i = -1; i <= 1; i++) {
            LocalDate d = dateTime.toLocalDate().plusDays(i);
            realizedTrips.addAll(trips.stream().filter(t -> t.isActive(d)).map(t -> {
                RealizedTrip realizedTrip = new RealizedTrip(t, d);
                injector.injectMembers(realizedTrip);
                return realizedTrip;
            }).collect(Collectors.toSet()));
        }
        System.out.println("realized trips: " + realizedTrips.size());

        List<RealizedPass> realizedPasses = new LinkedList<>();
        Set<String> edgeStops = Set.of(edge.getStop1().getParentStopId(), edge.getStop2().getParentStopId());
        System.out.println("edge stops: " + edgeStops);

        for (RealizedTrip realizedTrip : realizedTrips) {
            RealizedLocation previousLocation = null;
            List<RealizedLocation> realizedLocations = realizedTrip.getRealizedStopTimesWithIntermediate();
            for (RealizedLocation realizedLocation : realizedLocations) {
                if (previousLocation != null) {
                    if (previousLocation.getDeparture().isAfter(dateTime)) {
                        if (edgeStops.contains(previousLocation.getStop().getParentStopId()) && edgeStops.contains(realizedLocation.getStop().getParentStopId())) {
                            realizedPasses.add(new RealizedPass(realizedTrip, previousLocation, realizedLocation));
                        }
                    }
                }
                previousLocation = realizedLocation;
            }
        }

        System.out.println("realized passes: " + realizedPasses.size());
        Collections.sort(realizedPasses);


        return ok(views.html.timetable.edge.render(request, edge, realizedPasses, user));
    }
}
