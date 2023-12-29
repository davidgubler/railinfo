package entities;

import geometry.Point;

import java.util.List;

public class ReverseEdge implements Edge {
    private Edge edge;

    public ReverseEdge(Edge edge) {
        this.edge = edge;
    }

    public String getId() {
        return "-" + edge.getId();
    }

    @Override
    public String getIdReverse() {
        return edge.getId();
    }

    public void addJourney(Integer seconds) {
        edge.addJourney(seconds);
    }

    public Integer getTypicalTime() {
        return edge.getTypicalTime();
    }

    public String getStop1Id() {
        return edge.getStop2Id();
    }

    public Stop getStop1() {
        return edge.getStop2();
    }

    public String getStop2Id() {
        return edge.getStop1Id();
    }

    public Stop getStop2() {
        return edge.getStop1();
    }

    public Stop getDestination(Stop from) {
        return edge.getDestination(from);
    }

    public Point getStop1Coordinates() {
        return edge.getStop2Coordinates();
    }

    public Point getStop2Coordinates() {
        return edge.getStop1Coordinates();
    }

    public boolean isPrintable() {
        return edge.isPrintable();
    }

    public boolean isModified() {
        return edge.isModified();
    }

    public List<Point> getBoundingBox() {
        return edge.getBoundingBox();
    }

    public String toString(Stop from) {
        return edge.toString(from);
    }

    public Double getSpread(Point point) {
        return edge.getSpread(point);
    }

    @Override
    public int compareTo(Edge edge) {
        return this.edge.compareTo(edge);
    }

    @Override
    public boolean equals(Object o) {
        return edge.equals(o);
    }

    @Override
    public int hashCode() {
        return edge.hashCode();
    }

    @Override
    public String getDisplayName() {
        return getStop1().getName() + " - " + getStop2().getName();
    }
}
