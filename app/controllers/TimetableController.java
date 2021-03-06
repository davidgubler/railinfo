package controllers;

import com.google.inject.Inject;
import entities.Stop;
import entities.StopTime;
import entities.Trip;
import entities.realized.RealizedDeparture;
import entities.realized.RealizedTrip;
import models.*;
import play.mvc.Controller;
import play.mvc.Result;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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


    public Result departures(String stopStr)  {
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
            realizedTrips.addAll(trips.stream().filter(t -> t.isActive(d)).map(t -> new RealizedTrip(t, d, stopsModel)).collect(Collectors.toSet()));
        }

        List<RealizedDeparture> departures = new LinkedList<>();
        for (RealizedTrip realizedTrip : realizedTrips) {
            RealizedDeparture departure = realizedTrip.getDeparture(stops);
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

        return ok(views.html.timetable.stop.render(departures));
    }
}
