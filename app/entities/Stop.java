package entities;

import java.util.Map;

public class Stop {
    private final String id;
    private final String name;
    private final Double lat;
    private final Double lng;
    private final String type;
    private final String parentId;

    public Stop(Map<String, String> data) {
        this.id = data.get("stop_id");
        this.name = data.get("stop_name");
        this.lat = data.get("stop_lat").isBlank() ? null : Double.parseDouble(data.get("stop_lat"));
        this.lng = data.get("stop_lon").isBlank() ? null : Double.parseDouble(data.get("stop_lon"));
        this.type = data.get("location_type").isBlank() ? null : data.get("location_type");
        this.parentId = data.get("parent_station").isBlank() ? null : data.get("parent_station");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public String getType() {
        return type;
    }

    public String getParentId() {
        return parentId;
    }

    public String toString() {
        return name;
    }
}
