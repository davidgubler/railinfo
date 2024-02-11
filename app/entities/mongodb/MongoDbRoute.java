package entities.mongodb;

import configs.GtfsConfig;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.Objects;

@Entity(value = "routes", useDiscriminator = false)
public class MongoDbRoute implements entities.Route {
    @Id
    private ObjectId _id;

    @Indexed(options = @IndexOptions(unique = true))
    private String routeId;

    private String agencyId;

    private String shortName;

    private String longName;

    private String desc;

    private Integer type;

    @Transient
    private GtfsConfig gtfs;

    public MongoDbRoute() {
        // dummy constructor for morphia
    }

    public MongoDbRoute(Map<String, String> data) {
        this.routeId = data.get("route_id");
        this.agencyId = data.get("agency_id");
        this.shortName = data.get("route_short_name");
        this.longName = data.get("route_long_name");
        this.desc = data.get("route_desc");
        this.type = Integer.parseInt(data.get("route_type"));
    }

    public void setGtfs(GtfsConfig gtfs) {
        this.gtfs = gtfs;
    }

    @Override
    public String getProduct() {
        return gtfs.extractProduct(this);
    }

    @Override
    public String getRouteId() {
        return routeId;
    }

    @Override
    public String getAgencyId() {
        return agencyId;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public String getLongName() {
        return longName;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
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
        MongoDbRoute route = (MongoDbRoute) o;
        return Objects.equals(routeId, route.routeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeId);
    }
}
