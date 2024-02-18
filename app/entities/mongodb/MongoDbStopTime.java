package entities.mongodb;

import com.google.inject.Inject;
import configs.GtfsConfig;
import dev.morphia.annotations.*;
import entities.Stop;
import entities.StopTime;
import models.StopsModel;
import org.bson.types.ObjectId;

import java.util.Map;

@Entity(value = "stopTimes", useDiscriminator = false)
public class MongoDbStopTime implements StopTime {
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
    private GtfsConfig gtfs;

    public MongoDbStopTime() {
        // dummy constructor for morphia
    }

    public MongoDbStopTime(Map<String, String> data) {
        this.tripId = data.get("trip_id");
        this.arrival = data.get("arrival_time");
        this.departure = data.get("departure_time");
        this.stopId = data.get("stop_id");
        this.stopSequence = Integer.parseInt(data.get("stop_sequence"));
        this.pickupType = data.get("pickup_type");
        this.dropoffType = data.get("drop_off_type");
    }

    public void setGtfs(GtfsConfig gtfs) {
        this.gtfs = gtfs;
    }

    @Override
    public String getTripId() {
        return tripId;
    }

    @Override
    public String getStopId() {
        return stopId;
    }

    @Override
    public String getStopBaseId() {
        return gtfs.extractBaseId(this.getStopId());
    }

    @Override
    public Stop getStop() {
        return gtfs.getStopsModel().getByStopId(gtfs, stopId);
    }

    @Override
    public Integer getStopSequence() {
        return stopSequence;
    }

    @Override
    public String getArrival() {
        return arrival;
    }

    @Override
    public String getDeparture() {
        return departure;
    }

    @Override
    public GtfsConfig getSourceGtfs() {
        return gtfs;
    }

    @Override
    public int compareTo(StopTime stopTime) {
        return stopSequence.compareTo(stopTime.getStopSequence());
    }

    @Override
    public String toString() {
        return getStopId() + " " + getDeparture();
    }
}
