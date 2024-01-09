package entities.realized;

import entities.mongodb.MongoDbRoute;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RealizedDeparture implements Comparable<RealizedDeparture> {
    private RealizedTrip trip;

    private List<RealizedStopTime> realizedStopTimes;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public RealizedDeparture(RealizedTrip trip, List<RealizedStopTime> realizedStopTimes) {
        this.trip = trip;
        this.realizedStopTimes = realizedStopTimes;
    }

    public LocalDateTime getDepartureTime() {
        return realizedStopTimes.get(0).getDeparture();
    }

    public String getDepartureTimeStr() {
        return getDepartureTime().format(TIME_FORMATTER);
    }

    public String getHeadsign() {
        return trip.getTripHeadsign();
    }

    public List<RealizedStopTime> getStopTimes() {
        return realizedStopTimes;
    }

    public List<RealizedStopTime> getIntermediateStopTimes() {
        if (realizedStopTimes.size() <= 1) {
            return Collections.emptyList();
        }
        return realizedStopTimes.subList(1, realizedStopTimes.size() - 1);
    }

    public List<RealizedStopTime> getImportantIntermediateStopTimes(int count) {
        List<RealizedStopTime> intermediate = new LinkedList<>(getIntermediateStopTimes());
        if (intermediate.size() <= count) {
            return intermediate;
        }

        List<RealizedStopTime> byImportance = getIntermediateStopTimes();
        Collections.sort(byImportance, Comparator.comparingInt((RealizedStopTime a) -> a.getStop().getImportance()).reversed().thenComparing(RealizedStopTime::getDeparture));
        byImportance = byImportance.subList(0, count);

        Iterator<RealizedStopTime> it = intermediate.iterator();
        while (it.hasNext()) {
            RealizedStopTime stopTime = it.next();
            if (!byImportance.contains(stopTime)) {
                it.remove();
            }
        }
        return intermediate;
    }

    public RealizedStopTime getDestination() {
        return realizedStopTimes.get(realizedStopTimes.size() -1 );
    }

    public String getTrack() {
        String[] stopIdSplit = realizedStopTimes.get(0).getStopId().split(":");
        if (stopIdSplit.length == 3) {
            return stopIdSplit[2];
        }
        return null;
    }

    public MongoDbRoute getRoute() {
        return trip.getRoute();
    }

    public RealizedTrip getRealizedTrip() {
        return trip;
    }

    @Override
    public int compareTo(RealizedDeparture departure) {
        return getDepartureTime().compareTo(departure.getDepartureTime());
    }
}