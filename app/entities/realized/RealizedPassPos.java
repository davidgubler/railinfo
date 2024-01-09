package entities.realized;

import entities.Edge;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class RealizedPassPos implements Comparable<RealizedPassPos> {
    private final RealizedPass realizedPass;

    private Edge edge;

    private final double pos;


    public RealizedPassPos(RealizedPass realizedPass, Edge edge, double pos) {
        this.realizedPass = realizedPass;
        this.edge = edge;
        this.pos = pos;
    }

    public LocalDateTime getIntermediate() {
        return realizedPass.getIntermediate(realizedPass.isForward(edge.getStop1()), pos);
    }

    public Edge getEdge() {
        return edge;
    }

    public ZoneId getZoneId() {
        return realizedPass.getZoneId();
    }

    public boolean isForward() {
        return realizedPass.isForward(edge.getStop1());
    }

    public RealizedTrip getTrip() {
        return realizedPass.getTrip();
    }

    @Override
    public int compareTo(RealizedPassPos realizedPassPos) {
        return getIntermediate().compareTo(realizedPassPos.getIntermediate());
    }
}
