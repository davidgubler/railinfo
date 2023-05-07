package controllers;

import com.google.inject.Inject;
import com.google.inject.Injector;
import entities.*;
import entities.realized.*;
import models.*;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.MongoDb;
import utils.ErrorMessages;
import utils.InputUtils;
import utils.NotFoundException;
import utils.PathFinder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

    @Inject
    private MongoDb mongoDb;

    public Result index(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        return ok(views.html.timetable.index.render(request, null, InputUtils.NOERROR, user));
    }

    public Result indexPost(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        String databaseName = mongoDb.getTimetableDatabases("ch").get(0);

        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String submit = InputUtils.trimToNull(data.get("submit"));
        String stop = InputUtils.trimToNull(data.get("stop"));

        Map<String, String> errors = new HashMap<>();
        if ("Show Stop".equals(submit)) {
            if (stopsModel.getByName(databaseName, stop).isEmpty()) {
                errors.put("stop", ErrorMessages.STOP_NOT_FOUND);
            } else {
                return redirect(routes.TimetableController.stop(stop));
            }
        }

        return ok(views.html.timetable.index.render(request, stop, errors, user));
    }

    public Result stop(Http.Request request, String stopName)  {
        User user = usersModel.getFromRequest(request);
        String databaseName = mongoDb.getTimetableDatabases("ch").get(0);
        Set<? extends Stop> stops = stopsModel.getByName(databaseName, stopName);
        if (stops.isEmpty()) {
            throw new NotFoundException("Stop");
        }

        LocalDateTime dateTime = LocalDateTime.now();

        List<StopTime> stopTimes = stopTimesModel.getByStops(databaseName, stops);
        List<Trip> trips = new LinkedList<>();
        for (StopTime stopTime : stopTimes) {
            Trip trip = tripsModel.getByTripId(databaseName, stopTime.getTripId());
            if (trip != null) {
                trips.add(trip);
            }
        }

        Set<RealizedTrip> realizedTrips = realizeTrips(databaseName, dateTime.toLocalDate(), trips);

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

        return ok(views.html.timetable.stop.render(request, stopName, departures, user));
    }

    public Result realizedTrip(Http.Request request, String tripId, String startDateStr)  {
        User user = usersModel.getFromRequest(request);
        String databaseName = mongoDb.getTimetableDatabases("ch").get(0);
        LocalDate startDate = LocalDate.parse(startDateStr);
        Trip trip = tripsModel.getByTripId(databaseName, tripId);
        if (trip == null) {
            throw new NotFoundException("trip");
        }
        List<ServiceCalendarException> serviceCalendarExceptions = serviceCalendarExceptionsModel.getByServiceId(databaseName, trip.getServiceId());
        ServiceCalendar serviceCalendar = serviceCalendarsModel.getByServiceId(databaseName, trip.getServiceId());
        if (!trip.isActive(startDate, serviceCalendarExceptions, serviceCalendar)) {
            throw new NotFoundException("trip");
        }

        RealizedTrip realizedTrip = new RealizedTrip(trip, startDate);
        injector.injectMembers(realizedTrip);
        realizedTrip.setDatabaseName(databaseName);

        return ok(views.html.timetable.realizedTrip.render(request, realizedTrip, user));
    }

    private Set<RealizedTrip> realizeTrips(String databaseName, LocalDate d1, Collection<Trip> trips) {
        Set<RealizedTrip> realizedTrips = new HashSet<>();

        LocalDate d0 = d1.plusDays(-1);
        LocalDate d2 = d1.plusDays(1);

        Map<String, List<ServiceCalendarException>> serviceCalendarExceptionsByServiceId = serviceCalendarExceptionsModel.getByTripsAndDates(databaseName, trips, Arrays.asList(d0, d1, d2));
        Map<String, ServiceCalendar> serviceCalendarByServiceId = serviceCalendarsModel.getByTrips(databaseName, trips);
        for (Trip trip : trips) {
            List<ServiceCalendarException> serviceCalendarExceptions = serviceCalendarExceptionsByServiceId.get(trip.getServiceId());
            ServiceCalendar serviceCalendar = serviceCalendarByServiceId.get(trip.getServiceId());
            if (trip.isActive(d0, serviceCalendarExceptions, serviceCalendar)) {
                realizedTrips.add(new RealizedTrip(trip, d0));
            }
            if (trip.isActive(d1, serviceCalendarExceptions, serviceCalendar)) {
                realizedTrips.add(new RealizedTrip(trip, d1));
            }
            if (trip.isActive(d2, serviceCalendarExceptions, serviceCalendar)) {
                realizedTrips.add(new RealizedTrip(trip, d2));
            }
        }
        for (RealizedTrip realizedTrip : realizedTrips) {
            injector.injectMembers(realizedTrip);
            realizedTrip.setDatabaseName(databaseName);
        }
        return realizedTrips;
    }

    public Result edge(Http.Request request, String edgeId) {
        User user = usersModel.getFromRequest(request);
        String databaseName = mongoDb.getTimetableDatabases("ch").get(0);
        Edge edge = edgesModel.get(databaseName, edgeId);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }

        Set<String> routeIds = pathFinder.getRouteIdsByEdge(databaseName, edge);
        Set<Route> routes = new HashSet<>();
        for (String routeId : routeIds) {
            routes.add(routesModel.getByRouteId(databaseName, routeId));
        }

        Set<Trip> trips = new HashSet<>();
        for (Route route : routes) {
            trips.addAll(tripsModel.getByRoute(databaseName, route));
        }

        LocalDateTime dateTime = LocalDateTime.now();
        Set<RealizedTrip> realizedTrips = realizeTrips(databaseName, dateTime.toLocalDate(), trips);

        System.out.println("realized trips: " + realizedTrips.size());

        List<RealizedPass> realizedPasses = new LinkedList<>();
        Set<String> edgeStops = Set.of(edge.getStop1().getBaseId(), edge.getStop2().getBaseId());
        System.out.println("edge stops: " + edgeStops);

        for (RealizedTrip realizedTrip : realizedTrips) {
            RealizedLocation previousLocation = null;
            List<RealizedLocation> realizedLocations = realizedTrip.getRealizedStopTimesWithIntermediate();
            for (RealizedLocation realizedLocation : realizedLocations) {
                if (previousLocation != null) {
                    if (previousLocation.getDeparture().isAfter(dateTime)) {
                        if (edgeStops.contains(previousLocation.getStop().getBaseId()) && edgeStops.contains(realizedLocation.getStop().getBaseId())) {
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
