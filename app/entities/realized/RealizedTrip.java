package entities.realized;

import akka.actor.ProviderSelection;
import com.google.inject.Inject;
import entities.*;
import models.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RealizedTrip {

    private Trip trip;

    private LocalDate startDate;

    private List<RealizedStopTime> realizedStopTimes;

    @Inject
    private ServiceCalendarsModel serviceCalendarsModel;

    @Inject
    private ServiceCalendarExceptionsModel serviceCalendarExceptionsModel;

    @Inject
    private StopTimesModel stopTimesModel;

    @Inject
    private RoutesModel routesModel;

    @Inject
    private StopsModel stopsModel;

    public RealizedTrip(Trip trip, LocalDate startDate) {
        this.trip = trip;
        this.startDate = startDate;
    }

    public RealizedDeparture getDeparture(Collection<Stop> stops) {
        Set<String> stopIds = stops.stream().map(Stop::getStopId).collect(Collectors.toSet());
        int i = 0;
        for (RealizedStopTime realizedStopTime : getRealizedStopTimes()) {
            if (stopIds.contains(realizedStopTime.getStopId())) {
                break;
            }
            i++;
        }
        return new RealizedDeparture(this, getRealizedStopTimes().subList(i, realizedStopTimes.size()));
    }

    public List<RealizedStopTime> getRealizedStopTimes() {
        if (realizedStopTimes == null) {
            this.realizedStopTimes = Collections.unmodifiableList(trip.getStopTimes().stream().map(s -> new RealizedStopTime(s, startDate, stopsModel)).collect(Collectors.toList()));
            // At least in some cases the first and last stop have invalid arrivals/departures, fix this
            this.realizedStopTimes.get(0).setArrival(null);
            this.realizedStopTimes.get(realizedStopTimes.size()-1).setDeparture(null);
        }
        return new LinkedList<>(this.realizedStopTimes);
    }

    public List<RealizedStopTime> getRealizedStopTimesComplete() {
        List<RealizedStopTime> realizedStopTimes = getRealizedStopTimes();
        return realizedStopTimes;
    }

    public String getTripHeadsign() {
        return trip.getTripHeadsign();
    }

    public Trip getTrip() {
        return trip;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public Route getRoute() {
        return trip.getRoute();
    }
}
