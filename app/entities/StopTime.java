package entities;

import configs.GtfsConfig;

public interface StopTime extends Comparable<StopTime> {
    String getTripId();

    String getStopId();

    String getStopBaseId();

    Stop getStop();

    Integer getStopSequence();

    String getArrival();

    String getDeparture();

    GtfsConfig getSourceGtfs();
}
