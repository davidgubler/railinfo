package entities;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity(value = "edges", noClassnameStored = true)
public class Edge implements Comparable<Edge> {
    @Id
    private ObjectId _id;

    @Indexed
    private String fromStopId;
    private String toStopId;
    private Integer typicalTime = 0;

    @Transient
    private Map<Integer, Integer> travelTimes = new HashMap<>(); // key are seconds, value is #journeys

    public Edge() {
        // dummy constructor for morphia
    }

    public Edge(String fromStopId, String toStopId) {
        this.fromStopId = fromStopId;
        this.toStopId = toStopId;
    }

    public void addJourney(Integer seconds) {
        // we assume that a stop takes 1min 30s, thus we subtract this
        seconds -= 90;
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

    private Integer calculateTypicalTime() {
        int mostSeconds = 0, mostJourneys = 0;
        for (Map.Entry<Integer, Integer> entry : travelTimes.entrySet()) {
            if (entry.getValue() > mostJourneys) {
                mostSeconds = entry.getKey();
                mostJourneys = entry.getValue();
            }
        }
        return mostSeconds;
    }

    public String getFromStopId() {
        return fromStopId;
    }

    public String getToStopId() {
        return toStopId;
    }

    @Override
    public int compareTo(Edge edge) {
        return getTypicalTime().compareTo(edge.getTypicalTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Objects.equals(fromStopId, edge.fromStopId) && Objects.equals(toStopId, edge.toStopId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromStopId, toStopId);
    }
}