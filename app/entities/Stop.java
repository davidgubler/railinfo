package entities;

import geometry.Point;

public interface Stop extends Comparable<Stop> {
    String getId();

    String getStopId();

    String getName();

    String getNormalizedName();

    Double getLat();

    Double getLng();

    Point getCoordinates();

    String getType();

    String getBaseId();

    String getParentId();

    Integer getImportance();

    boolean isModified();
}
