package entities;

public interface Edge extends Comparable<Edge> {
    String getId();

    void addJourney(Integer seconds);

    Integer getTypicalTime();

    String getStop1Id();

    Stop getStop1();

    String getStop2Id();

    Stop getStop2();

    Stop getDestination(Stop from);

    Double getStop1Lat();

    Double getStop1Lng();

    Double getStop2Lat();

    Double getStop2Lng();

    boolean isPrintable();
}
