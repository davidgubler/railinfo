package entities;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;

import java.util.Map;

@Entity(value = "stopTimes", noClassnameStored = true)
public class StopTime implements Comparable<StopTime> {
    @Id
    private ObjectId _id;

    @Indexed
    private String tripId;

    private String arrival;

    private String departure;

    @Indexed
    private String stopId;

    private String stopSequence;

    private String pickupType;

    private String dropoffType;

    public StopTime() {
        // dummy constructor for morphia
    }

    public StopTime(Map<String, String> data) {
        this.tripId = data.get("trip_id");
        this.arrival = data.get("arrival_time");
        this.departure = data.get("departure_time");
        this.stopId = data.get("stop_id");
        this.stopSequence = data.get("stop_sequence");
        this.pickupType = data.get("pickup_type");
        this.dropoffType = data.get("drop_off_type");
    }

    public String getTripId() {
        return tripId;
    }

    public String getStopId() {
        return stopId;
    }

    public String getStopSequence() {
        return stopSequence;
    }

    @Override
    public int compareTo(StopTime stopTime) {
        return tripId.compareTo(stopTime.getTripId());
    }
}
