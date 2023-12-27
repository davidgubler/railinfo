package entities;

import geometry.Point;
import geometry.PolarCoordinates;

import java.util.Collection;

public class NearbyEdge implements Comparable<NearbyEdge> {
    private Edge edge;

    private Point point;

    private Double nearbyFactor = null;

    private double nearbyFactorNormalizer = 1.0;

    public NearbyEdge(Point point, Edge edge) {
        this.point = point;
        this.edge = edge;
    }

    public Double getNearbyFactor() {
        if (nearbyFactor == null) {
            double edgeLength = PolarCoordinates.distanceKm(edge.getStop1Coordinates(), edge.getStop2Coordinates());
            double distanceFromEdge = PolarCoordinates.distanceFromEdgeKm(point, edge.getStop1Coordinates(), edge.getStop2Coordinates());

            double d1 = PolarCoordinates.distanceKm(point, edge.getStop1Coordinates());
            double d2 = PolarCoordinates.distanceKm(point, edge.getStop2Coordinates());
            boolean overshoot1 = Math.sqrt(d2 * d2 - distanceFromEdge * distanceFromEdge) > edgeLength;
            boolean overshoot2 = Math.sqrt(d1 * d1 - distanceFromEdge * distanceFromEdge) > edgeLength;
            boolean between = !overshoot1 && !overshoot2;

            if (overshoot1) {
                distanceFromEdge = d1;
            }
            if (overshoot2) {
                distanceFromEdge = d2;
            }

            // There's not really much science to this but since you're looking...
            // We linearly decrease the factor from 1 (directly on the edge) to ca 1/3 of the edge length away. Everything beyond that is rated at 0.1.
            nearbyFactor = Math.max(1 - (3 * distanceFromEdge) / edgeLength, 0.1);

            // We divide by the log of the edge length to moderately de-prioritize long edges.
            nearbyFactor /= Math.log(edgeLength);

            if (!between) {
                // We want to allow very short distances from the corners to allow for imprecise localization but with exponential fall-off
                double distanceFromCorner = overshoot1 ? d1 : d2;
                nearbyFactor *= Math.pow(0.01, 5*distanceFromCorner);
            }
        }
        return nearbyFactor * nearbyFactorNormalizer;
    }

    public Double getPos() {
        double d1 = PolarCoordinates.distanceKm(point, edge.getStop1Coordinates());
        double d2 = PolarCoordinates.distanceKm(point, edge.getStop2Coordinates());
        return d1 / (d1 + d2);
    }

    public Edge getEdge() {
        return edge;
    }

    public void setNearbyFactorEqualizer(double nearbyFactorEqualizer) {
        this.nearbyFactorNormalizer = nearbyFactorEqualizer;
    }

    public static void normalizeNearbyFactors(Collection<NearbyEdge> nearbyEdges) {
        double sum = 0;
        for (NearbyEdge nearbyEdge : nearbyEdges) {
            sum += nearbyEdge.getNearbyFactor();
        }
        for (NearbyEdge nearbyEdge : nearbyEdges) {
            nearbyEdge.setNearbyFactorEqualizer(1.0/sum);
        }
    }

    @Override
    public int compareTo(NearbyEdge nearbyEdge) {
        return getNearbyFactor().compareTo(nearbyEdge.getNearbyFactor());
    }
}
