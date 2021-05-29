package entities;

import com.google.inject.Inject;
import dev.morphia.annotations.*;
import models.ServiceCalendarExceptionsModel;
import models.ServiceCalendarsModel;
import models.StopTimesModel;
import org.bson.types.ObjectId;

import java.util.Map;

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

    private String type;

    @Transient
    @Inject
    private ServiceCalendarsModel serviceCalendarsModel;

    @Transient
    @Inject
    private ServiceCalendarExceptionsModel serviceCalendarExceptionsModel;

    @Transient
    @Inject
    private StopTimesModel stopTimesModel;

    public Route() {
        // dummy constructor for morphia
    }

    public Route(Map<String, String> data) {
        this.routeId = data.get("route_id");
        this.agencyId = data.get("agency_id");
        this.shortName = data.get("route_short_name");
        this.longName = data.get("route_long_name");
        this.desc = data.get("route_desc");
        this.type = data.get("route_type");
    }

    public String routeId() {
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

    public String getType() {
        return type;
    }

    public String toString() {
        return routeId;
    }
}
