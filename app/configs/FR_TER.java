package configs;

import com.google.inject.Inject;
import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;
import entities.Route;
import entities.Trip;
import models.*;
import services.MongoDb;

import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FR_TER extends GtfsConfig {
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
        return "fr-ter";
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
        return new FR_TER(db, ds);
    }

    @Override
    public String getDownloadUrl() {
        return "https://eu.ftp.opendatasoft.com/sncf/gtfs/export-ter-gtfs-last.zip";
    }

    @Override
    public List<? extends Route> getRailRoutes() {
        return routesModel.getByType(this, 2, 3);
    }

    // group(1) is the train number, group(2) is F for train and R for bus
    private Pattern tripPattern = Pattern.compile("OCESN([0-9]+)([FR]).*");

    @Override
    public List<? extends Trip> getRailTripsByRoute(Route route) {
        // trips can return both trains and buses, therefore we have to remove the "R" trips (rue)
        List<? extends Trip> trips = tripsModel.getByRoute(route);
        Iterator<? extends Trip> iter = trips.iterator();
        while (iter.hasNext()) {
            Trip trip = iter.next();
            Matcher m = tripPattern.matcher(trip.getTripId());
            if (!m.matches()) {
                continue;
            }
            if ("R".equals(m.group(2))) {
                iter.remove();
            }
        }
        return trips;
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
    public String extractProduct(Route route) {
        return "R";
    }

    @Override
    public String extractLineName(Route route) {
        return "TER";
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

    public FR_TER() {
    }

    public FR_TER(MongoDatabase db, Datastore ds) {
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
