package entities;

public interface StopTime extends Comparable<StopTime> {
    String getTripId();

    String getStopId();

    String getStopBaseId();

    Stop getStop();

    Integer getStopSequence();

    String getArrival();

    String getDeparture();
}
