package entities.realized;

import entities.*;
import models.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RealizedTrip {

    private Trip trip;

    private LocalDate startDate;

    private List<RealizedStopTime> realizedStopTimes;

    private ServiceCalendarsModel serviceCalendarsModel;

    private ServiceCalendarExceptionsModel serviceCalendarExceptionsModel;

    private StopTimesModel stopTimesModel;

    private RoutesModel routesModel;

    public RealizedTrip(Trip trip, LocalDate startDate, StopsModel stopsModel) {
        this.trip = trip;
        this.startDate = startDate;
        this.realizedStopTimes = trip.getStopTimes().stream().map(s -> new RealizedStopTime(s, startDate, stopsModel)).collect(Collectors.toList());
    }

    public RealizedDeparture getDeparture(Collection<Stop> stops) {
        Set<String> stopIds = stops.stream().map(Stop::getStopId).collect(Collectors.toSet());
        int i = 0;
        for (RealizedStopTime realizedStopTime : realizedStopTimes) {
            if (stopIds.contains(realizedStopTime.getStopId())) {
                break;
            }
            i++;
        }
        return new RealizedDeparture(this, realizedStopTimes.subList(i, realizedStopTimes.size()));
    }

    public String getTripHeadsign() {
        return trip.getTripHeadsign();
    }

    public Route getRoute() {
        return trip.getRoute();
    }
}
