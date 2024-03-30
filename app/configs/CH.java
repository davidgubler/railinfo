package configs;

import com.google.inject.Inject;
import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;
import entities.Route;
import entities.Trip;
import models.*;
import services.MongoDb;

import java.time.ZoneId;
import java.util.List;

public class CH extends GtfsConfig {
    @Inject
    private StopsModel stopsModel;

    @Inject
    private StopTimesModel stopTimesModel;

    @Inject
    private RoutesModel routesModel;

    @Inject
    private ServiceCalendarsModel serviceCalendarsModel;

    @Inject
    private ServiceCalendarExceptionsModel serviceCalendarExceptionsModel;

    @Inject
    private TripsModel tripsModel;

    @Inject
    private EdgesModel edgesModel;

    @Override
    public ZoneId getZoneId() {
        return ZoneId.of("Europe/Zurich");
    }

    @Override
    public String getCode() {
        return "ch";
    }

    @Override
    public GtfsConfig withDatabase(MongoDb mongoDb, GtfsConfigModel gtfsConfigModel) {
        List<String> databases = mongoDb.getTimetableDatabases(getCode());
        if (databases.isEmpty()) {
            return this;
        }
        return withDatabase(mongoDb, databases.get(0), gtfsConfigModel);
    }

    @Override
    public GtfsConfig withDatabase(MongoDb mongoDb, String dbName, GtfsConfigModel gtfsConfigModel) {
        MongoDatabase db = mongoDb.get(dbName);
        Datastore ds = mongoDb.getDs(dbName);
        return new CH(db, ds);
    }

    @Override
    public String getDownloadUrl() {
        return "https://opentransportdata.swiss/de/dataset/timetable-2024-gtfs2020/permalink";
    }

    @Override
    public List<? extends Route> getRailRoutes() {
        return routesModel.getByType(this, 100, 199);
    }

    @Override
    public List<? extends Trip> getRailTripsByRoute(Route route) {
        return tripsModel.getByRoute(route); // no further filtering necessary as rail routes never contain bus trips
    }

    @Override
    public String extractBaseId(String stopId) {
        String baseId = stopId.split(":")[0];
        // base ID can contain stuff like "Parent" or "P"
        baseId = baseId.replaceAll("[^0-9]", "");
        return baseId;
    }

    @Override
    public String extractTrainNr(Trip trip) {
        return trip.getTripShortName();
    }

    @Override
    public String extractProduct(Route route) {
        return route.getDesc();
    }

    @Override
    public String extractLineName(Route route) {
        return route.getShortName();
    }

    @Override
    public int subtractStopTime(int edgeSeconds) {
        // we assume that a stop takes 1 min, thus we subtract this
        edgeSeconds -= 60;
        if (edgeSeconds < 30) {
            // the minimum assumed travel time between stops is 30s
            edgeSeconds = 30;
        }
        return edgeSeconds;
    }

    public CH() {
    }

    public CH(MongoDatabase db, Datastore ds) {
        this.db = db;
        this.ds = ds;
    }

    @Override
    public StopsModel getStopsModel() {
        return stopsModel;
    }

    @Override
    public StopTimesModel getStopTimesModel() {
        return stopTimesModel;
    }

    @Override
    public RoutesModel getRoutesModel() {
        return routesModel;
    }

    @Override
    public ServiceCalendarsModel getServiceCalendarsModel() {
        return serviceCalendarsModel;
    }

    @Override
    public ServiceCalendarExceptionsModel getServiceCalendarExceptionsModel() {
        return serviceCalendarExceptionsModel;
    }

    @Override
    public TripsModel getTripsModel() {
        return tripsModel;
    }

    @Override
    public EdgesModel getEdgesModel() {
        return edgesModel;
    }
}
