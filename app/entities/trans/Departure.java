package entities.trans;

import entities.Route;
import entities.Stop;
import entities.StopTime;
import entities.Trip;

import java.util.*;

public class Departure implements Comparable<Departure> {
    private Trip trip;

    private List<StopTime> stopTimes;

    public Departure(Trip trip, List<StopTime> stopTimes) {
        this.trip = trip;
        this.stopTimes = stopTimes;
    }

    public String getDepartureTime() {
        return stopTimes.get(0).getDeparture().substring(0, 5);
    }

    public String getHeadsign() {
        return trip.getTripHeadsign();
    }

    public List<StopTime> getStopTimes() {
        return stopTimes;
    }

    public List<StopTime> getIntermediateStopTimes() {
        if (stopTimes.size() <= 1) {
            return Collections.emptyList();
        }
        return stopTimes.subList(1, stopTimes.size() - 1);
    }

    public List<StopTime> getImportantIntermediateStopTimes(int count) {
        List<StopTime> intermediate = new LinkedList<>(getIntermediateStopTimes());
        if (intermediate.size() <= count) {
            return intermediate;
        }

        List<StopTime> byImportance = getIntermediateStopTimes();
        Collections.sort(byImportance, Comparator.comparingInt((StopTime a) -> a.getStop().getImportance()).reversed().thenComparing(StopTime::getDeparture));
        byImportance = byImportance.subList(0, count);

        Iterator<StopTime> it = intermediate.iterator();
        while (it.hasNext()) {
            StopTime stopTime = it.next();
            if (!byImportance.contains(stopTime)) {
                it.remove();
            }
        }
        return intermediate;
    }

    public StopTime getDestination() {
        return stopTimes.get(stopTimes.size() -1 );
    }

    public String getTrack() {
        String[] stopIdSplit = stopTimes.get(0).getStopId().split(":");
        if (stopIdSplit.length == 3) {
            return stopIdSplit[2];
        }
        return null;
    }

    public Route getRoute() {
        return trip.getRoute();
    }

    @Override
    public int compareTo(Departure departure) {
        return getDepartureTime().compareTo(departure.getDepartureTime());
    }
}