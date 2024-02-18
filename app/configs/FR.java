package configs;

import com.google.inject.Inject;
import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;
import entities.Route;
import entities.Trip;
import models.*;
import models.merged.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class FR implements GtfsConfig {
    private final List<GtfsConfig> subConfigs;

    @Inject
    private EdgesModel edgesModel;

    @Override
    public ZoneId getZoneId() {
        return ZoneId.of("Europe/Paris");
    }

    @Override
    public Datastore getDs() {
        return ds;
    }

    @Override
    public MongoDatabase getDatabase() {
        return db;
    }

    @Override
    public String getCode() {
        return "fr";
    }

    @Override
    public GtfsConfig withDatabase(MongoDatabase db, Datastore ds, GtfsConfigModel gtfsConfigModel) {
        if (db == null || ds == null) {
            return null;
        }
        List<GtfsConfig> subConfigsWithDatabase = new LinkedList<>();
        for (GtfsConfig subConfig : subConfigs) {
            GtfsConfig subConfigWithDatbase = gtfsConfigModel.getConfig(subConfig.getCode());
            if (subConfigWithDatbase == null) {
                return null;
            }
            subConfigsWithDatabase.add(subConfigWithDatbase);
        }
        return new FR(db, ds, subConfigsWithDatabase);
    }

    @Override
    public LocalDate getDate() {
        int i = db.getName().indexOf(getCode());
        return LocalDate.parse(db.getName().substring(i + getCode().length() + 1));
    }

    @Override
    public String getDownloadUrl() {
        return null;
    }

    @Override
    public List<? extends Route> getRailRoutes() {
        return new MergedRoutesModel(subConfigs).getRailRoutes();
    }

    @Override
    public List<? extends Trip> getRailTripsByRoute(Route route) {
        // FIXME this may not make sense
        return new MergedTripsModel(subConfigs).getRailTripsByRoute(route);
    }

    @Override
    public String extractBaseId(String stopId) {
        throw new IllegalStateException();
    }

    @Override
    public String extractTrainNr(Trip trip) {
        throw new IllegalStateException();
    }

    @Override
    public String extractProduct(Route route) {
        throw new IllegalStateException();
    }

    @Override
    public String extractLineName(Route route) {
        throw new IllegalStateException();
    }

    @Override
    public int subtractStopTime(int edgeSeconds) {
        throw new IllegalStateException();
    }

    private MongoDatabase db;
    private Datastore ds;

    public FR() {
        subConfigs = List.of(new FR_IC(), new FR_TER());
    }

    public FR(MongoDatabase db, Datastore ds, List<GtfsConfig> subConfigs) {
        this.db = db;
        this.ds = ds;
        this.subConfigs = subConfigs;
    }

    @Override
    public StopsModel getStopsModel() {
        return new MergedStopsModel(subConfigs);
    }

    @Override
    public StopTimesModel getStopTimesModel() {
        return new MergedStopTimesModel(subConfigs);
    }

    @Override
    public RoutesModel getRoutesModel() {
        return new MergedRoutesModel(subConfigs);
    }

    @Override
    public ServiceCalendarsModel getServiceCalendarsModel() {
        return new MergedServiceCalendarsModel(subConfigs);
    }

    @Override
    public ServiceCalendarExceptionsModel getServiceCalendarExceptionsModel() {
        return new MergedServiceCalendarExceptionsModel(subConfigs);
    }

    @Override
    public TripsModel getTripsModel() {
        return new MergedTripsModel(subConfigs);
    }

    @Override
    public EdgesModel getEdgesModel() {
        return edgesModel;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public String toString() {
        return db == null ? "railinfo-" + getCode() : db.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FR fr = (FR) o;
        return Objects.equals(db.getName(), fr.db.getName());
    }

    @Override
    public int hashCode() {
        return db.getName().hashCode();
    }
}
