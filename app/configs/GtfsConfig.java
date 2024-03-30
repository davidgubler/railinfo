package configs;

import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;
import entities.LocalDateRange;
import entities.Route;
import entities.Trip;
import models.*;
import services.MongoDb;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

public abstract class GtfsConfig implements Comparable<GtfsConfig> {

    protected MongoDatabase db;
    protected Datastore ds;

    public abstract ZoneId getZoneId();

    public abstract String getCode();

    public abstract GtfsConfig withDatabase(MongoDb mongoDb, GtfsConfigModel gtfsConfigModel);

    public abstract GtfsConfig withDatabase(MongoDb mongoDb, String databaseName, GtfsConfigModel gtfsConfigModel);

    public abstract String getDownloadUrl();

    public abstract String extractBaseId(String stopId);

    public abstract String extractTrainNr(Trip trip);

    public abstract String extractProduct(Route route);

    public abstract String extractLineName(Route route);

    public abstract int subtractStopTime(int edgeSeconds);

    public abstract List<? extends Route> getRailRoutes();

    public abstract List<? extends Trip> getRailTripsByRoute(Route route);

    public abstract StopsModel getStopsModel();

    public abstract RoutesModel getRoutesModel();

    public abstract ServiceCalendarsModel getServiceCalendarsModel();

    public abstract ServiceCalendarExceptionsModel getServiceCalendarExceptionsModel();

    public abstract TripsModel getTripsModel();

    public abstract StopTimesModel getStopTimesModel();

    public abstract EdgesModel getEdgesModel();

    public boolean isEditable() {
        return true;
    }

    public MongoDatabase getDatabase() {
        return db;
    }

    public Datastore getDs() {
        return ds;
    }

    public LocalDate getDate() {
        if (db == null) {
            return null;
        }
        int i = db.getName().indexOf(getCode());
        return LocalDate.parse(db.getName().substring(i + getCode().length() + 1));
    }

    private LocalDateRange calendarsDateRange = null;

    private LocalDateRange getCalendarsDateRange() {
        if (calendarsDateRange == null) {
            calendarsDateRange = getServiceCalendarsModel().getDateRange(this);
        }
        return calendarsDateRange;
    }

    private LocalDateRange calendarExceptionsDateRange = null;

    private LocalDateRange getCalendarExceptionsDateRange() {
        if (calendarExceptionsDateRange == null) {
            calendarExceptionsDateRange = getServiceCalendarExceptionsModel().getDateRange(this);
        }
        return calendarExceptionsDateRange;
    }

    public LocalDate getStart() {
        if (getCalendarsDateRange() != null) {
            return getCalendarsDateRange().getStart();
        }
        if (getCalendarExceptionsDateRange() != null) {
            return getCalendarExceptionsDateRange().getStart();
        }
        return null;
    }

    public LocalDate getEnd() {
        if (getCalendarsDateRange() != null) {
            return getCalendarsDateRange().getEnd();
        }
        if (getCalendarExceptionsDateRange() != null) {
            return getCalendarExceptionsDateRange().getEnd();
        }
        return null;
    }

    @Override
    public String toString() {
        return getDatabase() == null ? "railinfo-" + getCode() : getDatabase().getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GtfsConfig otherconfig = (GtfsConfig) o;
        return Objects.equals(db.getName(), otherconfig.db.getName());
    }

    @Override
    public int hashCode() {
        return db.getName().hashCode();
    }

    @Override
    public int compareTo(GtfsConfig other) {
        return db.getName().compareTo(other.db.getName());
    }
}
