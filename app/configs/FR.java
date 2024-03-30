package configs;

import com.google.inject.Inject;
import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;
import entities.Route;
import entities.Trip;
import models.*;
import models.merged.*;
import services.MongoDb;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class FR extends GtfsConfig {
    private final List<GtfsConfig> subConfigs;

    @Inject
    private EdgesModel edgesModel;

    @Override
    public ZoneId getZoneId() {
        return ZoneId.of("Europe/Paris");
    }

    @Override
    public String getCode() {
        return "fr";
    }

    @Override
    public GtfsConfig withDatabase(MongoDb mongoDb, GtfsConfigModel gtfsConfigModel) {
        List<GtfsConfig> subConfigsWithDatabase = new LinkedList<>();
        for (GtfsConfig subConfig : subConfigs) {
            subConfig = gtfsConfigModel.getConfig(subConfig.getCode());
            if (subConfig.getDatabase() == null) {
                return this;
            }
            subConfigsWithDatabase.add(subConfig);
        }

        LocalDate maxDate = Collections.max(subConfigsWithDatabase.stream().map(GtfsConfig::getDate).collect(Collectors.toList()));
        String dbName = "railinfo-fr-" + maxDate;
        MongoDatabase db = mongoDb.get(dbName);
        Datastore ds = mongoDb.getDs(dbName);

        return new FR(db, ds, subConfigsWithDatabase);
    }

    @Override
    public GtfsConfig withDatabase(MongoDb mongoDb, String dbName, GtfsConfigModel gtfsConfigModel) {
        List<GtfsConfig> subConfigsWithDatabase = new LinkedList<>();
        // FIXME we get the latest subconfigs here instead of the ones that fit the dbName. This is OK for now but will need to be fixed at some point.
        for (GtfsConfig subConfig : subConfigs) {
            subConfig = gtfsConfigModel.getConfig(subConfig.getCode());
            if (subConfig.getDatabase() == null) {
                return this;
            }
            subConfigsWithDatabase.add(subConfig);
        }
        MongoDatabase db = mongoDb.get(dbName);
        Datastore ds = mongoDb.getDs(dbName);
        return new FR(db, ds, subConfigsWithDatabase);
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

    public FR() {
        this.subConfigs = List.of(new FR_IC(), new FR_TER());
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
}
