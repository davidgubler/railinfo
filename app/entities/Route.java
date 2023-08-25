package entities;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.Objects;

@Entity(value = "routes", noClassnameStored = true)
public class Route {
    @Id
    private ObjectId _id;

    @Indexed(options = @IndexOptions(unique = true))
    private String routeId;

    private String agencyId;

    private String shortName;

    private String longName;

    private String desc;

    private Integer type;

    public Route() {
        // dummy constructor for morphia
    }

    public Route(Map<String, String> data) {
        this.routeId = data.get("route_id");
        this.agencyId = data.get("agency_id");
        this.shortName = data.get("route_short_name");
        this.longName = data.get("route_long_name");
        this.desc = data.get("route_desc");
        this.type = Integer.parseInt(data.get("route_type"));
    }

    public String getRouteId() {
        return routeId;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public String getDesc() {
        return desc;
    }

    public Integer getType() {
        return type;
    }

    public String toString() {
        return routeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(routeId, route.routeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeId);
    }
}
