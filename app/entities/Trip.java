package entities;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;

import java.util.Map;

@Entity(value = "trips", noClassnameStored = true)
public class Trip {
    @Id
    private ObjectId _id;

    @Indexed(options = @IndexOptions(unique = true))
    private final String tripId;

    @Indexed
    private final String routeId;

    @Indexed
    private final String serviceId;

    private final String tripHeadsign;

    @Indexed
    private final String tripShortName;

    private final String directionId;

    public Trip(Map<String, String> data) {
        this.routeId = data.get("route_id");
        this.serviceId = data.get("service_id");
        this.tripId = data.get("trip_id");
        this.tripHeadsign = data.get("trip_headsign");
        this.tripShortName = data.get("trip_short_name");
        this.directionId = data.get("direction_id");
    }

    public String routeId() {
        return routeId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getTripId() {
        return tripId;
    }

    public String getTripHeadsign() {
        return tripHeadsign;
    }

    public String getTripShortName() {
        return tripShortName;
    }

    public String getDirectionId() {
        return directionId;
    }


    public String toString() {
        return tripId;
    }
}
