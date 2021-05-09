package controllers;

import com.google.inject.Inject;
import entities.Stop;
import entities.StopTime;
import entities.Trip;
import entities.trans.Departure;
import models.*;
import play.mvc.Controller;
import play.mvc.Result;

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


    public Result stop(String stopStr)  {
        Set<Stop> stops = stopsModel.getByName(stopStr);

        List<StopTime> stopTimes = stopTimesModel.getByStops(stops);
        System.out.println("found " + stopTimes.size() + " stop times");

        List<Trip> trips = new LinkedList<>();

        for (StopTime stopTime : stopTimes) {
            Trip trip = tripsModel.getByTripId(stopTime.getTripId());
            if (trip != null) {
                trips.add(trip);
            }
        }

        System.out.println("found " + trips.size() + " trips");

        trips = trips.stream().filter(t -> t.isActiveToday()).collect(Collectors.toList());
        Collections.sort(trips);

        System.out.println("found " + trips.size() + " trips for today");
        List<Departure> departures = new LinkedList<>();
        for (Trip trip : trips) {
            Departure departure = trip.getDeparture(stops);
            departures.add(departure);
        }
        Collections.sort(departures);
        for (Departure departure : departures) {
            System.out.println(departure.getDepartureTime() + "   " + departure.getHeadsign());
        }

        return ok();
    }
}
