package entities;

import java.util.Map;

public class StopTime {
    private final String tripId;
    private final String arrival;
    private final String departure;
    private final String stopId;
    private final String stopSequence;
    private final String pickupType;
    private final String dropoffType;

    public StopTime(Map<String, String> data) {
        this.tripId = data.get("trip_id");
        this.arrival = data.get("arrival_time");
        this.departure = data.get("departure_time");
        this.stopId = data.get("stop_id");
        this.stopSequence = data.get("stop_sequence");
        this.pickupType = data.get("pickup_type");
        this.dropoffType = data.get("drop_off_type");
    }
}
