package entities;

import geometry.Point;

import java.util.List;

public interface Edge extends Comparable<Edge> {
    String getId();

    void addJourney(Integer seconds);

    Integer getTypicalTime();

    String getStop1Id();

    Stop getStop1();

    String getStop2Id();

    Stop getStop2();

    Stop getDestination(Stop from);

    Point getStop1Coordinates();

    Point getStop2Coordinates();

    boolean isPrintable();

    boolean isModified();

    boolean isDisabled();

    List<Point> getBoundingBox();

    String toString(Stop from);

    Double getSpread(Point point);

    String getDisplayName();

    String getNormalizedName();

    String getNormalizedNameReverse();
}
