package geometry;

import entities.Edge;
import entities.Stop;

import java.util.Comparator;

public class EdgeDirectionComparator implements Comparator<Edge> {

    private Stop from;
    private Double targetBearing = null;

    public EdgeDirectionComparator(Stop from, Stop to) {
        this.from = from;
        if (from.getCoordinates() != null && to.getCoordinates() != null) {
            targetBearing = getBearing(from, to);
        }
    }

    private double getBearing(Stop from, Stop to) {
        return PolarCoordinates.bearingDegrees(from.getCoordinates(), to.getCoordinates());
    }

    public double getTargetBearing() {
        return targetBearing;
    }

    @Override
    public int compare(Edge e1, Edge e2) {
        if (targetBearing == null) {
            return 0;
        }
        double e1BearingDiff = PolarCoordinates.bearingDiff(targetBearing, getBearing(from, e1.getDestination(from)));
        double e2BearingDiff = PolarCoordinates.bearingDiff(targetBearing, getBearing(from, e2.getDestination(from)));
        if (e1BearingDiff > e2BearingDiff) {
            return 1;
        }
        if (e1BearingDiff < e2BearingDiff) {
            return -1;
        }
        return 0;
    }
}
