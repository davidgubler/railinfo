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
import java.util.stream.Stream;

public class FR extends GtfsConfig {
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
        return ZoneId.of("Europe/Paris");
    }

    @Override
    public String getCode() {
        return "fr";
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
        return new FR(db, ds);
    }

    @Override
    public String getDownloadUrl() {
        return "https://eu.ftp.opendatasoft.com/sncf/plandata/Export_OpenData_SNCF_GTFS_NewTripId.zip";
    }

    @Override
    public List<? extends Route> getRailRoutes() {
        return routesModel.getByType(this, 2, 2);
    }

    @Override
    public List<? extends Trip> getRailTripsByRoute(Route route) {
        Stream<? extends Trip> trips = tripsModel.getByRoute(route);
        trips = trips.filter(t -> {
            String[] split = t.getTripId().split(":");
            return split.length >= 2 && !"CTE".equals(split[1]);
        });
        return trips.toList();
    }

    @Override
    public String extractBaseId(String stopId) {
        if (stopId.contains("-")) {
            return stopId.substring(stopId.lastIndexOf("-") + 1);
        }
        if (stopId.contains("OCE")) {
            return stopId.substring(stopId.lastIndexOf("OCE") + 3);
        }
        return stopId;
    }

    @Override
    public String extractTrainNr(Trip trip) {
        return trip.getTripHeadsign();
    }

    @Override
    public String extractProduct(Trip trip) {
        try {
            String[] split = trip.getTripId().split(":");
            if ("OUI".equals(split[1])) {
                return "TGV";
            }
            if ("OGO".equals(split[1])) {
                return "OUIGO";
            }
            return split[1];
        } catch (Exception e) {
            return "??";
        }
    }

    @Override
    public String extractLineName(Trip trip) {
        return extractProduct(trip);
    }

    @Override
    public int subtractStopTime(int edgeSeconds) {
        // we assume that a stop takes 2 min, thus we subtract this
        edgeSeconds -= 120;
        if (edgeSeconds < 30) {
            // the minimum assumed travel time between stops is 30s
            edgeSeconds = 30;
        }
        return edgeSeconds;
    }

    public FR() {
    }

    public FR(MongoDatabase db, Datastore ds) {
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
