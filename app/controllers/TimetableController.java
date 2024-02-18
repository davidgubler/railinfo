package controllers;

import com.google.inject.Inject;
import com.google.inject.Injector;
import configs.GtfsConfig;
import entities.*;
import entities.realized.*;
import models.*;
import play.mvc.Http;
import play.mvc.Result;
import services.MongoDb;
import utils.ErrorMessages;
import utils.InputUtils;
import utils.NotFoundException;
import geometry.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TimetableController extends GtfsController {

    @Inject
    private EdgesModel edgesModel;

    @Inject
    private UsersModel usersModel;

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    @Inject
    private RealizerModel realizerModel;

    @Inject
    private GtfsConfigModel gtfsConfigModel;

    public Result index(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);
        return ok(views.html.timetable.index.render(request, null, null, InputUtils.NOERROR, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result indexPost(Http.Request request, String cc) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);

        Map<String, String[]> data = request.body().asFormUrlEncoded();
        String submit = InputUtils.trimToNull(data.get("submit"));
        String stop = InputUtils.trimToNull(data.get("stop"));
        String coordinates = InputUtils.trimToNull(data.get("coordinates"));

        Map<String, String> errors = new HashMap<>();
        if ("Show Departures".equals(submit)) {
            if (gtfs.getStopsModel().getByName(gtfs, stop).isEmpty()) {
                errors.put("stop", ErrorMessages.STOP_NOT_FOUND);
            } else {
                return redirect(routes.TimetableController.stop(gtfs.getCode(), stop));
            }
        }
        if ("Show Nearby Trains".equals(submit)) {
            if (Point.fromString(coordinates) == null) {
                errors.put("coordinates", ErrorMessages.PLEASE_ENTER_VALID_COORDINATES);
            } else {
                return redirect(routes.TimetableController.nearby(gtfs.getCode(), coordinates));
            }
        }

        return ok(views.html.timetable.index.render(request, stop, coordinates, errors, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result nearby(Http.Request request, String cc, String coordinates) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);

        Point point = Point.fromString(coordinates);
        if (point == null) {
            throw new NotFoundException("coordinates");
        }
        List<NearbyEdge> nearbyEdges = edgesModel.getByPoint(gtfs, point);
        List<NearbyEdge> nearbyEdgesLikely = nearbyEdges.stream().filter(ne -> ne.getNearbyFactor() > 0.1).collect(Collectors.toList());
        List<NearbyEdge> nearbyEdgesUnlikely = nearbyEdges.stream().filter(ne -> ne.getNearbyFactor() <= 0.1).collect(Collectors.toList());
        return ok(views.html.timetable.nearby.render(request, point, nearbyEdgesLikely, nearbyEdgesUnlikely, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result stop(Http.Request request, String cc, String stopName)  {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);

        Set<? extends Stop> stops = gtfs.getStopsModel().getByName(gtfs, stopName);
        if (stops.isEmpty()) {
            throw new NotFoundException("Stop");
        }

        LocalDateTime dateTime = LocalDateTime.now(gtfs.getZoneId()).minusMinutes(15);

        List<? extends StopTime> stopTimes = gtfs.getStopTimesModel().getByStops(gtfs, stops);
        List<Trip> trips = new LinkedList<>();
        for (StopTime stopTime : stopTimes) {
            Trip trip = gtfs.getTripsModel().getByTripId(gtfs, stopTime.getTripId());
            if (trip != null) {
                trips.add(trip);
            }
        }

        Set<RealizedTrip> realizedTrips = realizerModel.realizeTrips(gtfs, dateTime.toLocalDate(), trips);

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

        return ok(views.html.timetable.stop.render(request, stopName, departures, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result realizedTrip(Http.Request request, String cc, String tripId, String startDateStr)  {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);

        LocalDate startDate = LocalDate.parse(startDateStr);
        Trip trip = gtfs.getTripsModel().getByTripId(gtfs, tripId);
        if (trip == null) {
            throw new NotFoundException("trip");
        }
        List<? extends ServiceCalendarException> serviceCalendarExceptions = gtfs.getServiceCalendarExceptionsModel().getByTrip(trip);
        ServiceCalendar serviceCalendar = gtfs.getServiceCalendarsModel().getByTrip(trip);
        if (!trip.isActive(startDate, serviceCalendarExceptions, serviceCalendar)) {
            throw new NotFoundException("trip");
        }

        RealizedTrip realizedTrip = new RealizedTrip(trip, startDate);
        injector.injectMembers(realizedTrip);
        realizedTrip.setGtfs(gtfs);

        return ok(views.html.timetable.realizedTrip.render(request, realizedTrip, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result edge(Http.Request request, String cc, String edgeName) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);

        Edge edge = edgesModel.getByName(gtfs, edgeName);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }

        LocalDateTime dateTime = LocalDateTime.now(gtfs.getZoneId()).minusMinutes(15);
        List<RealizedPass> realizedPasses = realizerModel.getPasses(gtfs, edge, dateTime);

        return ok(views.html.timetable.edge.render(request, edge, realizedPasses, null, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }

    public Result edgePos(Http.Request request, String cc, String edgeName, Double pos) {
        User user = usersModel.getFromRequest(request);
        GtfsConfig gtfs = gtfsConfigModel.getConfig(cc);
        check(gtfs);

        Edge edge = edgesModel.getByName(gtfs, edgeName);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }

        List<RealizedPass> realizedPasses = realizerModel.getPasses(gtfs, edge, LocalDateTime.now(gtfs.getZoneId()).minusMinutes(15));
        Collections.sort(realizedPasses, new RealizedPassIntermediateComparator(edge.getStop1(), pos));

        return ok(views.html.timetable.edge.render(request, edge, realizedPasses, pos, user, gtfsConfigModel.getSelectorChoices(), gtfs));
    }
}
