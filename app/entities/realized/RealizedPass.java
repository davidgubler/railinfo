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
        if (startEdge.getStop().getBaseId().equals(stop.getBaseId())) {
            return startEdge;
        }
        if (endEdge.getStop().getBaseId().equals(stop.getBaseId())) {
            return endEdge;
        }
        return null;
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
