package entities.mongodb;

import com.google.inject.Inject;
import dev.morphia.annotations.*;
import entities.Edge;
import entities.Stop;
import models.StopsModel;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity(value = "edges", noClassnameStored = true)
public class MongoDbEdge implements Edge, Comparable<Edge> {
    @Id
    private ObjectId _id;

    @Indexed
    private String stop1Id;
    @Indexed
    private String stop2Id;
    private Integer typicalTime = 0;

    @Transient
    private Stop stop1;
    @Transient
    private Stop stop2;

    @Transient
    private Map<Integer, Integer> travelTimes = new HashMap<>(); // key are seconds, value is #journeys

    @Transient
    @Inject
    private StopsModel stopsModel;

    public MongoDbEdge() {
        // dummy constructor for morphia
    }

    public MongoDbEdge(String stop1Id, String stop2Id) {
        this.stop1Id = stop1Id;
        this.stop2Id = stop2Id;
    }

    public String getId() {
        return _id.toString();
    }

    public ObjectId getObjectId() {
        return _id;
    }

    public void addJourney(Integer seconds) {
        // we assume that a stop takes 1 min, thus we subtract this
        seconds -= 60;
        if (seconds < 30) {
            // the minimum assumed travel time between stops is 30s
            seconds = 30;
        }
        if (travelTimes.containsKey(seconds)) {
            travelTimes.put(seconds, travelTimes.get(seconds) + 1);
        } else {
            travelTimes.put(seconds, 1);
        }

        typicalTime = calculateTypicalTime();
    }

    public Integer getTypicalTime() {
        return typicalTime;
    }

    public void setTypicalTime(int typicalTime) {
        this.typicalTime = typicalTime;
    }

    private Integer calculateTypicalTime() {
        int smallest = Integer.MAX_VALUE;
        for (Map.Entry<Integer, Integer> entry : travelTimes.entrySet()) {
            if (entry.getKey() < smallest) {
                smallest = entry.getKey();
            }
        }
        return smallest < 30 ? 30 : smallest;
    }

    public String getStop1Id() {
        return stop1Id;
    }

    public Stop getStop1() {
        if (stop1 == null) {
            stop1 = stopsModel.getById(stop1Id);
        }
        return stop1;
    }

    public String getStop2Id() {
        return stop2Id;
    }

    public Stop getStop2() {
        if (stop2== null) {
            stop2 = stopsModel.getById(stop2Id);
        }
        return stop2;
    }

    public Stop getDestination(Stop from) {
        if (getStop1Id().equals(from.getParentStopId())) {
            return getStop2();
        }
        if (getStop2Id().equals(from.getParentStopId())) {
            return getStop1();
        }
        return null;
    }

    public Double getStop1Lat() {
        return getStop1() == null ? null : getStop1().getLat();
    }

    public Double getStop1Lng() {
        return getStop1() == null ? null : getStop1().getLng();
    }

    public Double getStop2Lat() {
        return getStop2() == null ? null : getStop2().getLat();
    }

    public Double getStop2Lng() {
        return getStop2() == null ? null : getStop2().getLng();
    }

    public boolean isPrintable() {
        return getStop1Lat() != null && getStop1Lng() != null && getStop2Lat() != null && getStop2Lng() != null;
    }

    @Override
    public int compareTo(Edge edge) {
        return getTypicalTime().compareTo(edge.getTypicalTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoDbEdge edge = (MongoDbEdge) o;
        return Objects.equals(stop1Id, edge.stop1Id) && Objects.equals(stop2Id, edge.stop2Id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stop1Id, stop2Id);
    }

    @Override
    public String toString() {
        return getStop1().getName() + "[" + getStop1().getParentStopId() + "] ---" + getTypicalTime() + "s--> " + getStop2().getName() + "[" + getStop2().getParentStopId() + "]";
    }

    @Override
    public String toString(Stop from) {
        return from.getName() + "[" + from.getParentStopId() + "] ---" + getTypicalTime() + "s--> " + getDestination(from).getName() + "[" + getDestination(from).getParentStopId() + "]";
    }
}