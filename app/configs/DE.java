package configs;

import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;
import entities.Route;
import entities.Trip;
import models.RoutesModel;
import models.TripsModel;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

public class DE implements GtfsConfig {
    @Override
    public ZoneId getZoneId() {
        return ZoneId.of("Europe/Berlin");
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
        return "de";
    }

    @Override
    public GtfsConfig withDatabase(MongoDatabase db, Datastore ds) {
        if (db == null || ds == null) {
            return null;
        }
        return new configs.DE(db, ds);
    }

    @Override
    public LocalDate getDate() {
        int i = db.getName().indexOf(getCode());
        return LocalDate.parse(db.getName().substring(i + getCode().length() + 1));
    }

    @Override
    public String getDownloadUrl() {
        return "https://download.gtfs.de/germany/free/latest.zip";
    }

    @Override
    public List<? extends Route> getRailRoutes(RoutesModel routesModel) {
        return routesModel.getByType(this, 2, 2);
    }

    @Override
    public List<? extends Trip> getRailTripsByRoute(TripsModel tripsModel, Route route) {
        return tripsModel.getByRoute(this, route); // no further filtering necessary as rail routes never contain bus trips
    }

    @Override
    public String extractBaseId(String stopId) {
        String baseId = stopId.split(":")[0];
        // base ID can contain stuff like "Parent" or "P"
        baseId = baseId.replaceAll("[^0-9]", "");
        return baseId;
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

    public DE() {
    }

    public DE(MongoDatabase db, Datastore ds) {
        this.db = db;
        this.ds = ds;
    }

    @Override
    public String toString() {
        return db.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        configs.DE de = (configs.DE) o;
        return Objects.equals(db.getName(), de.db.getName());
    }

    @Override
    public int hashCode() {
        return db.getName().hashCode();
    }

}
