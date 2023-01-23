package entities.realized;

import entities.Stop;

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
        if (startEdge.getStop().equals(stop)) {
            return startEdge;
        }
        if (endEdge.getStop().equals(stop)) {
            return endEdge;
        }
        return null;
    }

    public boolean isForward(Stop stop) {
        return startEdge.getStop().equals(stop);
    }

    public RealizedTrip getTrip() {
        return trip;
    }

    @Override
    public int compareTo(RealizedPass realizedPass) {
        return startEdge.getDeparture().compareTo(realizedPass.startEdge.getDeparture());
    }
}
