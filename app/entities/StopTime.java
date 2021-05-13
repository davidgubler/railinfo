package entities;

import com.google.inject.Inject;
import dev.morphia.annotations.*;
import models.StopTimesModel;
import models.StopsModel;
import org.bson.types.ObjectId;

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

    private Integer stopSequence;

    private String pickupType;

    private String dropoffType;

    @Transient
    @Inject
    private StopsModel stopsModel;

    public StopTime() {
        // dummy constructor for morphia
    }

    public StopTime(Map<String, String> data) {
        this.tripId = data.get("trip_id");
        this.arrival = data.get("arrival_time");
        this.departure = data.get("departure_time");
        this.stopId = data.get("stop_id");
        this.stopSequence = Integer.parseInt(data.get("stop_sequence"));
        this.pickupType = data.get("pickup_type");
        this.dropoffType = data.get("drop_off_type");
    }

    public String getTripId() {
        return tripId;
    }

    public String getStopId() {
        return stopId;
    }

    public Stop getStop() {
        return stopsModel.getById(stopId);
    }

    public Integer getStopSequence() {
        return stopSequence;
    }

    public String getDeparture() {
        return departure;
    }

    @Override
    public int compareTo(StopTime stopTime) {
        return tripId.compareTo(stopTime.getTripId());
    }

    @Override
    public String toString() {
        return getStopId() + " " + getDeparture();
    }
}
