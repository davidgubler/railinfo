package controllers;

import com.google.inject.Inject;
import com.google.inject.Injector;
import configs.GtfsConfig;
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
import geometry.*;

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
        return ok(views.html.timetable.index.render(request, null, null, InputUtils.NOERROR, user));
    }

    public Result indexPost(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");

        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String submit = InputUtils.trimToNull(data.get("submit"));
        String stop = InputUtils.trimToNull(data.get("stop"));
        String coordinates = InputUtils.trimToNull(data.get("coordinates"));

        Map<String, String> errors = new HashMap<>();
        if ("Show Departures".equals(submit)) {
            if (stopsModel.getByName(gtfs, stop).isEmpty()) {
                errors.put("stop", ErrorMessages.STOP_NOT_FOUND);
            } else {
                return redirect(routes.TimetableController.stop(stop));
            }
        }
        if ("Show Nearby Trains".equals(submit)) {
            if (Point.fromString(coordinates) == null) {
                errors.put("coordinates", ErrorMessages.PLEASE_ENTER_VALID_COORDINATES);
            } else {
                return redirect(routes.TimetableController.nearby(coordinates));
            }
        }

        return ok(views.html.timetable.index.render(request, stop, coordinates, errors, user));
    }

    public Result nearby(Http.Request request, String coordinates) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Point point = Point.fromString(coordinates);
        if (point == null) {
            throw new NotFoundException("coordinates");
        }

        List<? extends Edge> edges = edgesModel.getByPoint(gtfs, point);

        Map<Edge, Double> pos = new HashMap<>();
        for (Edge edge : edges) {
            double d1 = PolarCoordinates.distanceKm(point, edge.getStop1Coordinates());
            double d2 = PolarCoordinates.distanceKm(point, edge.getStop2Coordinates());
            pos.put(edge, d1 / (d1 + d2));
        }

        return ok(views.html.timetable.nearby.render(request, point, edges, pos, user));
    }

    public Result stop(Http.Request request, String stopName)  {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Set<? extends Stop> stops = stopsModel.getByName(gtfs, stopName);
        if (stops.isEmpty()) {
            throw new NotFoundException("Stop");
        }

        LocalDateTime dateTime = LocalDateTime.now();

        List<StopTime> stopTimes = stopTimesModel.getByStops(gtfs, stops);
        List<Trip> trips = new LinkedList<>();
        for (StopTime stopTime : stopTimes) {
            Trip trip = tripsModel.getByTripId(gtfs, stopTime.getTripId());
            if (trip != null) {
                trips.add(trip);
            }
        }

        Set<RealizedTrip> realizedTrips = realizeTrips(gtfs, dateTime.toLocalDate(), trips);

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
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        LocalDate startDate = LocalDate.parse(startDateStr);
        Trip trip = tripsModel.getByTripId(gtfs, tripId);
        if (trip == null) {
            throw new NotFoundException("trip");
        }
        List<ServiceCalendarException> serviceCalendarExceptions = serviceCalendarExceptionsModel.getByServiceId(gtfs, trip.getServiceId());
        ServiceCalendar serviceCalendar = serviceCalendarsModel.getByServiceId(gtfs, trip.getServiceId());
        if (!trip.isActive(startDate, serviceCalendarExceptions, serviceCalendar)) {
            throw new NotFoundException("trip");
        }

        RealizedTrip realizedTrip = new RealizedTrip(trip, startDate);
        injector.injectMembers(realizedTrip);
        realizedTrip.setGtfs(gtfs);

        return ok(views.html.timetable.realizedTrip.render(request, realizedTrip, user));
    }

    private Set<RealizedTrip> realizeTrips(GtfsConfig gtfs, LocalDate d1, Collection<Trip> trips) {
        Set<RealizedTrip> realizedTrips = new HashSet<>();

        LocalDate d0 = d1.plusDays(-1);
        LocalDate d2 = d1.plusDays(1);

        Map<String, List<ServiceCalendarException>> serviceCalendarExceptionsByServiceId = serviceCalendarExceptionsModel.getByTripsAndDates(gtfs, trips, Arrays.asList(d0, d1, d2));
        Map<String, ServiceCalendar> serviceCalendarByServiceId = serviceCalendarsModel.getByTrips(gtfs, trips);
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
            realizedTrip.setGtfs(gtfs);
        }
        return realizedTrips;
    }

    private List<RealizedPass> getPasses(GtfsConfig gtfs, Edge edge) {
        Set<String> routeIds = pathFinder.getRouteIdsByEdge(gtfs, edge);
        Set<Route> routes = new HashSet<>();
        for (String routeId : routeIds) {
            routes.add(routesModel.getByRouteId(gtfs, routeId));
        }

        Set<Trip> trips = new HashSet<>();
        for (Route route : routes) {
            trips.addAll(tripsModel.getByRoute(gtfs, route));
        }

        LocalDateTime dateTime = LocalDateTime.now();
        Set<RealizedTrip> realizedTrips = realizeTrips(gtfs, dateTime.toLocalDate(), trips);

        List<RealizedPass> realizedPasses = new LinkedList<>();
        Set<String> edgeStops = Set.of(edge.getStop1().getBaseId(), edge.getStop2().getBaseId());

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
        Collections.sort(realizedPasses);
        return realizedPasses;
    }

    public Result edge(Http.Request request, String edgeId) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Edge edge = edgesModel.get(gtfs, edgeId);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }

        List<RealizedPass> realizedPasses = getPasses(gtfs, edge);

        return ok(views.html.timetable.edge.render(request, edge, realizedPasses, null, user));
    }

    public Result edgePos(Http.Request request, String edgeId, Double pos) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = mongoDb.getLatest("ch");
        Edge edge = edgesModel.get(gtfs, edgeId);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }

        List<RealizedPass> realizedPasses = getPasses(gtfs, edge);
        Collections.sort(realizedPasses, new RealizedPassIntermediateComparator(edge.getStop1(), pos));

        return ok(views.html.timetable.edge.render(request, edge, realizedPasses, pos, user));
    }
}
