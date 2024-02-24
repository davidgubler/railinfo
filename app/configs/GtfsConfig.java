package configs;

import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;
import entities.Route;
import entities.Trip;
import models.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public interface GtfsConfig {
    ZoneId getZoneId();

    Datastore getDs();

    MongoDatabase getDatabase();

    String getCode();

    GtfsConfig withDatabase(MongoDatabase db, Datastore ds, GtfsConfigModel gtfsConfigModel);

    LocalDate getDate();

    String getDownloadUrl();

    String extractBaseId(String stopId);

    String extractTrainNr(Trip trip);

    String extractProduct(Route route);

    String extractLineName(Route route);

    int subtractStopTime(int edgeSeconds);

    List<? extends Route> getRailRoutes();

    List<? extends Trip> getRailTripsByRoute(Route route);

    StopsModel getStopsModel();

    RoutesModel getRoutesModel();

    ServiceCalendarsModel getServiceCalendarsModel();

    ServiceCalendarExceptionsModel getServiceCalendarExceptionsModel();

    TripsModel getTripsModel();

    StopTimesModel getStopTimesModel();

    EdgesModel getEdgesModel();

    default boolean isEditable() {
        return true;
    }
}
