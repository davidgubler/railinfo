package models;

import com.google.inject.Inject;
import configs.*;
import configs.GtfsConfig;
import services.MongoDb;

import java.util.LinkedList;
import java.util.List;

public class GtfsConfigModel {

    @Inject
    private MongoDb mongoDb;

    private static List<GtfsConfig> COUNTRIES = List.of(new CH(), new DE(), new FR_TER());

    public GtfsConfig getConfig(String cc) {
        if (cc == null) {
            return null;
        }
        for (GtfsConfig country : COUNTRIES) {
            if (country.getCode().equals(cc)) {
                List<String> databases = mongoDb.getTimetableDatabases(country.getCode());
                if (databases.isEmpty()) {
                    return country;
                } else {
                    GtfsConfig gtfs = country.withDatabase(mongoDb.get(databases.get(0)), mongoDb.getDs(databases.get(0)));
                    return gtfs;
                }
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
                choices.add(country.withDatabase(mongoDb.get(databases.get(0)), mongoDb.getDs(databases.get(0))));
            }
            for (String database : databases) {
                secondaryChoices.add(country.withDatabase(mongoDb.get(database), mongoDb.getDs(database)));
            }
        }

        secondaryChoices.stream().map(c -> choices.add(c));
        return choices;
    }
}
