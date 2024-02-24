package configs;

import com.google.inject.Inject;
import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;
import entities.Route;
import entities.Trip;
import models.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

public class CH implements GtfsConfig {
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
    public Datastore getDs() {
        return ds;
    }

    @Override
    public MongoDatabase getDatabase() {
        return db;
    }

    @Override
    public String getCode() {
        return "ch";
    }

    @Override
    public GtfsConfig withDatabase(MongoDatabase db, Datastore ds, GtfsConfigModel gtfsConfigModel) {
        if (db == null || ds == null) {
            return null;
        }
        return new CH(db, ds);
    }

    @Override
    public LocalDate getDate() {
        int i = db.getName().indexOf(getCode());
        return LocalDate.parse(db.getName().substring(i + getCode().length() + 1));
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

    private MongoDatabase db;
    private Datastore ds;

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

    @Override
    public String toString() {
        return db.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CH ch = (CH) o;
        return Objects.equals(db.getName(), ch.db.getName());
    }

    @Override
    public int hashCode() {
        return db.getName().hashCode();
    }
}
