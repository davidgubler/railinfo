package models;

import com.google.inject.Inject;
import com.google.inject.Injector;
import configs.*;
import configs.GtfsConfig;
import services.MongoDb;

import java.util.LinkedList;
import java.util.List;

public class GtfsConfigModel {
    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    public static List<GtfsConfig> COUNTRIES = List.of(new CH(), new FR(), new UZ());

    public GtfsConfig getConfig(String cc) {
        if (cc == null) {
            return null;
        }
        for (GtfsConfig country : COUNTRIES) {
            if (country.getCode().equals(cc)) {
                GtfsConfig gtfs = country.withDatabase(mongoDb, this);
                injector.injectMembers(gtfs);
                return gtfs;
            }
        }
        return null;
    }

    public List<GtfsConfig> getSelectorChoices() {
        List<GtfsConfig> choices = new LinkedList<>();
        List<GtfsConfig> secondaryChoices = new LinkedList<>();

        for (GtfsConfig country : COUNTRIES) {
            List<String> databases = mongoDb.getTimetableDatabases(country.getCode());
            if (databases.isEmpty()) {
                choices.add(country);
            } else {
                choices.add(country.withDatabase(mongoDb, this));
            }
            for (String database : databases) {
                secondaryChoices.add(country.withDatabase(mongoDb, this));
            }
        }

        secondaryChoices.stream().map(c -> choices.add(c));
        return choices;
    }
}
