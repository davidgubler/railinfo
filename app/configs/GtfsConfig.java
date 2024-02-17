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

public interface GtfsConfig {
    ZoneId getZoneId();

    Datastore getDs();

    MongoDatabase getDatabase();

    String getCode();

    GtfsConfig withDatabase(MongoDatabase db, Datastore ds);

    LocalDate getDate();

    String getDownloadUrl();

    String extractBaseId(String stopId);

    String extractTrainNr(Trip trip);

    String extractProduct(Route route);

    String extractLineName(Route route);

    int subtractStopTime(int edgeSeconds);

    List<? extends Route> getRailRoutes(RoutesModel routesModel);

    List<? extends Trip> getRailTripsByRoute(TripsModel tripsModel, Route route);
}
