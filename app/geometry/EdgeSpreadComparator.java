package geometry;

import entities.Edge;

import java.util.Comparator;

public class EdgeSpreadComparator implements Comparator<Edge> {

    private Point point;

    public EdgeSpreadComparator(Point point) {
        this.point = point;
    }

    @Override
    public int compare(Edge e1, Edge e2) {
        Double spread1 = e1.getSpread(point);
        Double spread2 = e2.getSpread(point);
        if (spread1 == null || spread2 == null) return 0;
        return spread2.compareTo(spread1);
    }
}
