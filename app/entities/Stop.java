package entities;

import java.util.List;

public class Stop {
    private final String id;
    private final String name;
    private final Double lat;
    private final Double lng;
    private final String type;
    private final String parentId;

    public Stop(List<String> data) {
        this.id = data.get(0);
        this.name = data.get(1);
        this.lat = data.get(2).isBlank() ? null : Double.parseDouble(data.get(2));
        this.lng = data.get(3).isBlank() ? null : Double.parseDouble(data.get(3));
        this.type = data.get(4).isBlank() ? null : data.get(4);
        this.parentId = data.get(5).isBlank() ? null : data.get(5);
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
