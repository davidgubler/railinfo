package entities.realized;

import entities.Stop;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class RealizedPass implements Comparable<RealizedPass> {
    private RealizedTrip trip;
    private RealizedLocation startEdge;
    private RealizedLocation endEdge;

    public RealizedPass(RealizedTrip trip, RealizedLocation start, RealizedLocation end) {
        this.trip = trip;
        this.startEdge = start;
        this.endEdge = end;
    }

    public RealizedLocation get(Stop stop) {
        if (startEdge.getStop().getBaseId().equals(stop.getBaseId())) {
            return startEdge;
        }
        if (endEdge.getStop().getBaseId().equals(stop.getBaseId())) {
            return endEdge;
        }
        return null;
    }

    public LocalDateTime getIntermediate(boolean isForward, Double pos) {
        if (!isForward) {
            pos = 1.0 - pos;
        }
        long edgeSeconds = startEdge.getDeparture().until(endEdge.getArrival(), ChronoUnit.SECONDS);
        return startEdge.getDeparture().plusSeconds(Math.round(edgeSeconds * pos));
    }

    public boolean isForward(Stop stop) {
        return startEdge.getStop().getBaseId().equals(stop.getBaseId());
    }

    public RealizedTrip getTrip() {
        return trip;
    }

    @Override
    public int compareTo(RealizedPass realizedPass) {
        return startEdge.getDeparture().compareTo(realizedPass.startEdge.getDeparture());
    }
}
