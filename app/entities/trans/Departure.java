package entities.trans;

import entities.StopTime;
import entities.Trip;

import java.util.List;

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

    @Override
    public int compareTo(Departure departure) {
        return getDepartureTime().compareTo(departure.getDepartureTime());
    }
}
