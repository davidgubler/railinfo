package entities;

import geometry.Point;

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

    String toString(Stop from);
}
