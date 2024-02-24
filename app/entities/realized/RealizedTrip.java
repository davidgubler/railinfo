package entities.realized;

import biz.Topology;
import com.google.inject.Inject;
import configs.GtfsConfig;
import entities.*;
import entities.Stop;
import entities.mongodb.MongoDbRoute;
import utils.PathFinder;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RealizedTrip {

    private Trip trip;

    private LocalDate startDate;

    private List<RealizedStopTime> realizedStopTimes;

    private GtfsConfig gtfs;

    @Inject
    private Topology topology;

    @Inject
    private PathFinder pathFinder;

    public RealizedTrip(Trip trip, LocalDate startDate) {
        this.trip = trip;
        this.startDate = startDate;
    }

    public void setGtfs(GtfsConfig gtfs) {
        this.gtfs = gtfs;
    }

    public RealizedDeparture getDeparture(Collection<? extends Stop> stops) {
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
            this.realizedStopTimes = Collections.unmodifiableList(trip.getStopTimes().stream().map(s -> new RealizedStopTime(s, startDate, gtfs)).collect(Collectors.toList()));
            // At least in some cases the first and last stop have invalid arrivals/departures, fix this
            this.realizedStopTimes.get(0).setArrival(null);
            this.realizedStopTimes.get(realizedStopTimes.size()-1).setDeparture(null);
        }
        return new LinkedList<>(this.realizedStopTimes);
    }

    private List<RealizedLocation> realizedStopTimesWithIntermediate = null;

    public List<RealizedLocation> getRealizedStopTimesWithIntermediate() {
        if (realizedStopTimesWithIntermediate == null) {
            List<RealizedStopTime> realizedStopTimes = getRealizedStopTimes();

            List<RealizedLocation> complete = new LinkedList<>();
            complete.add(realizedStopTimes.get(0));
            for (int i = 1; i < realizedStopTimes.size(); i++) {
                Stop from = realizedStopTimes.get(i - 1).getStop();
                Stop to = realizedStopTimes.get(i).getStop();
                complete.addAll(pathFinder.getIntermediate(gtfs, from, to, realizedStopTimes.get(i - 1).getDeparture(), realizedStopTimes.get(i).getArrival()));
                complete.add(realizedStopTimes.get(i));
            }
            realizedStopTimesWithIntermediate = complete;
        }
        return realizedStopTimesWithIntermediate;
    }

    public String getTripHeadsign() {
        return trip.getTripHeadsign();
    }

    public String getTripShortName() {
        return trip.getTripShortName();
    }

    public Trip getTrip() {
        return trip;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public MongoDbRoute getRoute() {
        return trip.getRoute();
    }

    public Stop getBegins() {
        return realizedStopTimesWithIntermediate.get(0).getStop();
    }

    public Stop getEnds() {
        return realizedStopTimesWithIntermediate.get(realizedStopTimesWithIntermediate.size() - 1).getStop();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RealizedTrip that = (RealizedTrip) o;
        return Objects.equals(trip, that.trip) && Objects.equals(startDate, that.startDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trip, startDate);
    }
}
