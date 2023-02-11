package entities;

import com.google.inject.Inject;
import models.RoutesModel;
import models.ServiceCalendarExceptionsModel;
import models.ServiceCalendarsModel;
import models.StopTimesModel;
import org.bson.types.ObjectId;
import dev.morphia.annotations.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity(value = "trips", noClassnameStored = true)
public class Trip implements Comparable<Trip> {
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
    private String databaseName;

    @Transient
    @Inject
    private ServiceCalendarsModel serviceCalendarsModel;

    @Transient
    @Inject
    private ServiceCalendarExceptionsModel serviceCalendarExceptionsModel;

    @Transient
    @Inject
    private StopTimesModel stopTimesModel;

    @Transient
    @Inject
    private RoutesModel routesModel;

    public Trip() {
        // dummy constructor for morphia
    }

    public Trip(Map<String, String> data) {
        this.routeId = data.get("route_id");
        this.serviceId = data.get("service_id");
        this.tripId = data.get("trip_id");
        this.tripHeadsign = data.get("trip_headsign");
        this.tripShortName = data.get("trip_short_name");
        this.directionId = data.get("direction_id");
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
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

    public boolean isActive(LocalDate date) {
        List<ServiceCalendarException> serviceCalendarExceptions = serviceCalendarExceptionsModel.getByServiceId(databaseName, serviceId);
        List<ServiceCalendarException> todaysExceptions = serviceCalendarExceptions.stream().filter(s -> date.equals(s.getDate())).collect(Collectors.toList());
        if (todaysExceptions.size() == 1) {
            return todaysExceptions.get(0).getActive();
        }

        ServiceCalendar cal = serviceCalendarsModel.getByServiceId(databaseName, serviceId);
        if (date.isBefore(cal.getStart()) || date.isAfter(cal.getEnd())) {
            return false;
        }

        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.MONDAY && cal.getMonday()) {
            return true;
        }
        if (dow == DayOfWeek.TUESDAY && cal.getTuesday()) {
            return true;
        }
        if (dow == DayOfWeek.WEDNESDAY && cal.getWednesday()) {
            return true;
        }
        if (dow == DayOfWeek.THURSDAY && cal.getThursday()) {
            return true;
        }
        if (dow == DayOfWeek.FRIDAY && cal.getFriday()) {
            return true;
        }
        if (dow == DayOfWeek.SATURDAY && cal.getSaturday()) {
            return true;
        }
        if (dow == DayOfWeek.SUNDAY && cal.getSunday()) {
            return true;
        }
        return false;
    }

    public List<StopTime> getStopTimes() {
        return stopTimesModel.getByTrip(databaseName, this);
    }

    public Route getRoute() {
        return routesModel.getByRouteId(databaseName, routeId);
    }

    @Override
    public int compareTo(Trip trip) {
        return tripShortName.compareTo(trip.getTripShortName());
    }
}
