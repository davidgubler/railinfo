package configs;

import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;
import entities.Route;
import entities.Trip;
import models.RoutesModel;
import models.TripsModel;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FR_IC implements GtfsConfig {
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
        return "fr-ic";
    }

    @Override
    public GtfsConfig withDatabase(MongoDatabase db, Datastore ds) {
        if (db == null || ds == null) {
            return null;
        }
        return new FR_IC(db, ds);
    }

    @Override
    public LocalDate getDate() {
        int i = db.getName().indexOf(getCode());
        return LocalDate.parse(db.getName().substring(i + getCode().length() + 1));
    }

    @Override
    public String getDownloadUrl() {
        return "https://eu.ftp.opendatasoft.com/sncf/gtfs/export-intercites-gtfs-last.zip";
    }

    @Override
    public List<? extends Route> getRailRoutes(RoutesModel routesModel) {
        return routesModel.getByType(this, 2, 2);
    }

    // group(1) is the train number, group(2) is F for train and R for bus
    private Pattern tripPattern = Pattern.compile("OCESN([0-9]+)([FR]).*");

    @Override
    public List<? extends Trip> getRailTripsByRoute(TripsModel tripsModel, Route route) {
        // trips can return both trains and buses, therefore we have to remove the "R" trips (rue)
        List<? extends Trip> trips = tripsModel.getByRoute(this, route);
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
        Matcher m = tripPattern.matcher(trip.getTripId());
        if (m.matches()) {
            return m.group(1);
        }
        return "";
    }

    @Override
    public String extractProduct(Route route) {
        return "IC";
    }

    @Override
    public String extractLineName(Route route) {
        return "IC " + route.getShortName();
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

    private MongoDatabase db;
    private Datastore ds;

    public FR_IC() {

    }

    public FR_IC(MongoDatabase db, Datastore ds) {
        this.db = db;
        this.ds = ds;
    }

    @Override
    public String toString() {
        return db == null ? "railinfo-" + getCode() : db.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FR_IC frTer = (FR_IC) o;
        return Objects.equals(db.getName(), frTer.db.getName());
    }

    @Override
    public int hashCode() {
        return db.getName().hashCode();
    }
}
