package entities.mongodb;

import com.google.inject.Inject;
import configs.GtfsConfig;
import entities.*;
import models.RoutesModel;
import models.StopTimesModel;
import org.bson.types.ObjectId;
import dev.morphia.annotations.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity(value = "trips", useDiscriminator = false)
public class MongoDbTrip implements Trip {
    @Id
    private ObjectId _id;

    @Indexed(options = @IndexOptions(unique = true))
    private String tripId;

    @Indexed
    private String routeId;

    @Indexed
    private String serviceId;

    private String tripHeadsign;

    @Indexed
    private String tripShortName;

    private String directionId;

    @Transient
    private GtfsConfig gtfs;

    @Transient
    @Inject
    private StopTimesModel stopTimesModel;

    @Transient
    @Inject
    private RoutesModel routesModel;

    public MongoDbTrip() {
        // dummy constructor for morphia
    }

    public MongoDbTrip(Map<String, String> data) {
        this.routeId = data.get("route_id");
        this.serviceId = data.get("service_id");
        this.tripId = data.get("trip_id");
        this.tripHeadsign = data.get("trip_headsign");
        this.tripShortName = data.get("trip_short_name");
        this.directionId = data.get("direction_id");
    }

    public void setGtfs(GtfsConfig gtfs) {
        this.gtfs = gtfs;
    }

    public String routeId() {
        return routeId;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getTripId() {
        return tripId;
    }

    @Override
    public String getTripHeadsign() {
        return tripHeadsign;
    }

    @Override
    public String getTripShortName() {
        return tripShortName;
    }

    @Override
    public String getTrainNr() {
        return gtfs.extractTrainNr(this);
    }

    @Override
    public String getDirectionId() {
        return directionId;
    }

    public String toString() {
        return tripId;
    }

    @Override
    public boolean isActive(LocalDate date, List<? extends ServiceCalendarException> serviceCalendarExceptions, ServiceCalendar serviceCalendar) {
        if (serviceCalendarExceptions == null) {
            serviceCalendarExceptions = Collections.emptyList();
        }
        List<ServiceCalendarException> todaysExceptions = serviceCalendarExceptions.stream().filter(s -> date.equals(s.getDate())).collect(Collectors.toList());
        if (todaysExceptions.size() == 1) {
            return todaysExceptions.get(0).getActive();
        }

        if (serviceCalendar == null) {
            // there may be no calendar for services that run very sporadically
            return false;
        }

        if (date.isBefore(serviceCalendar.getStart()) || date.isAfter(serviceCalendar.getEnd())) {
            return false;
        }

        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.MONDAY && serviceCalendar.getMonday()) {
            return true;
        }
        if (dow == DayOfWeek.TUESDAY && serviceCalendar.getTuesday()) {
            return true;
        }
        if (dow == DayOfWeek.WEDNESDAY && serviceCalendar.getWednesday()) {
            return true;
        }
        if (dow == DayOfWeek.THURSDAY && serviceCalendar.getThursday()) {
            return true;
        }
        if (dow == DayOfWeek.FRIDAY && serviceCalendar.getFriday()) {
            return true;
        }
        if (dow == DayOfWeek.SATURDAY && serviceCalendar.getSaturday()) {
            return true;
        }
        if (dow == DayOfWeek.SUNDAY && serviceCalendar.getSunday()) {
            return true;
        }
        return false;
    }

    @Override
    public List<? extends StopTime> getStopTimes() {
        return stopTimesModel.getByTrip(gtfs, this);
    }

    @Override
    public MongoDbRoute getRoute() {
        return routesModel.getByRouteId(gtfs, routeId);
    }

    @Override
    public int compareTo(Trip trip) {
        return tripShortName.compareTo(trip.getTripShortName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoDbTrip that = (MongoDbTrip) o;
        return Objects.equals(tripId, that.tripId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId);
    }
}
