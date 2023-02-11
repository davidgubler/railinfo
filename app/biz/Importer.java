package biz;

import com.google.inject.Inject;
import entities.User;
import models.*;
import services.MongoDb;
import utils.*;
import play.mvc.Http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Importer {

    @Inject
    private StopsModel stopsModel;

    @Inject
    private StopTimesModel stopTimesModel;

    @Inject
    private TripsModel tripsModel;

    @Inject
    private RoutesModel routesModel;

    @Inject
    private ServiceCalendarsModel serviceCalendarsModel;

    @Inject
    private ServiceCalendarExceptionsModel serviceCalendarExceptionsModel;

    @Inject
    private MongoDb mongoDb;

    public static final String UTF8_BOM = "\uFEFF";

    public void importGtfs(Http.RequestHeader request, String urlStr, String databaseName, User user) throws InputValidationException {
        // ACCESS
        if (user == null) {
            throw new NotAllowedException();
        }

        // INPUT
        Map<String, String> errors = new HashMap<>();
        InputUtils.validateUrl(urlStr, "url", true, errors);
        InputUtils.validateString(databaseName, "databaseName", true, errors);
        if (databaseName != null && !databaseName.startsWith("railinfo-ch-")) {
            errors.put("databaseName", ErrorMessages.PLEASE_ENTER_VALID_DATABASE_NAME);
        }
        if (mongoDb.getTimetableDatabases("ch").contains(databaseName)) {
            errors.put("databaseName", ErrorMessages.PLEASE_ENTER_DIFFERENT_NAME);
        }
        if (!errors.isEmpty()) {
            throw new InputValidationException(errors);
        }

        new Thread(() -> {
            try {
                // BUSINESS
                long start = System.currentTimeMillis();
                URL url = new URL(urlStr);
                ZipInputStream zipIn = new ZipInputStream(url.openStream());
                ZipEntry entry = zipIn.getNextEntry();
                int stops = 0, trips = 0, routes = 0, stopTimes = 0, serviceCalendars = 0, serviceCalendarExceptions = 0;
                while (entry != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    RailinfoLogger.info(request, "importing " + entry.getName());

                    InputStreamReader zipInReader = new InputStreamReader(zipIn);
                    BufferedReader reader = new BufferedReader(zipInReader);

                    if ("stops.txt".equals(entry.getName())) {
                        stopsModel.drop(databaseName);
                        stops = parseFile(zipIn, dataMap -> stopsModel.create(databaseName, dataMap));
                    } else if ("trips.txt".equals(entry.getName())) {
                        tripsModel.drop(databaseName);
                        trips = parseFile(zipIn, dataMap -> tripsModel.create(databaseName, dataMap));
                    } else if ("routes.txt".equals(entry.getName())) {
                        routesModel.drop(databaseName);
                        routes = parseFile(zipIn, dataMap -> routesModel.create(databaseName, dataMap));
                    } else if ("stop_times.txt".equals(entry.getName())) {
                        stopTimesModel.drop(databaseName);
                        stopTimes = parseFile(zipIn, dataMap -> stopTimesModel.create(databaseName, dataMap));
                    } else if ("calendar.txt".equals(entry.getName())) {
                        serviceCalendarsModel.drop(databaseName);
                        serviceCalendars = parseFile(zipIn, dataMap -> serviceCalendarsModel.create(databaseName, dataMap));
                    } else if ("calendar_dates.txt".equals(entry.getName())) {
                        serviceCalendarExceptionsModel.drop(databaseName);
                        serviceCalendarExceptions = parseFile(zipIn, dataMap -> serviceCalendarExceptionsModel.create(databaseName, dataMap));
                    } else {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // just skip it
                        }
                    }
                    zipIn.closeEntry();
                    entry = zipIn.getNextEntry();
                }
                zipIn.close();

                // LOG
                RailinfoLogger.info(request, user + " imported database " + databaseName + " in " + (System.currentTimeMillis() - start) + " ms");
                RailinfoLogger.info(request, "found " + stops + " stops");
                RailinfoLogger.info(request, "found " + trips + " trips");
                RailinfoLogger.info(request, "found " + routes + " routes");
                RailinfoLogger.info(request, "found " + stopTimes + " stopTimes");
                RailinfoLogger.info(request, "found " + serviceCalendars + " serviceCalendars");
                RailinfoLogger.info(request, "found " + serviceCalendarExceptions + " serviceCalendarExceptions");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String[] parseLine(String line, int length) {
        String[] components = new String[length];
        int i = 0;
        int pos = 0;
        while (pos < line.length()) {
            if ("\"".equals(line.substring(pos, pos + 1))) {
                int endPos = line.indexOf("\"", pos + 1);
                if (endPos == -1) {
                    endPos = line.length();
                }
                components[i++] = line.substring(pos + 1, endPos);
                pos = Math.min(endPos + 2, line.length());
            } else {
                int endPos = line.indexOf(",", pos + 1);
                if (endPos == -1) {
                    endPos = line.length();
                }
                components[i++] = line.substring(pos, endPos);
                pos = Math.min(endPos + 1, line.length());
            }
        }
        if (i < length) {
            components = Arrays.copyOf(components, i);
        }
        return components;
    }

    private <T> int parseFile(InputStream is, Function<List<Map<String, String>>, List<T>> creator) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String[] header = parseLine(reader.readLine(), 100);
        if (header[0].startsWith(UTF8_BOM)) {
            // nobody likes a byte order mark
            header[0] = header[0].substring(1);
        }
        int batchSize = 100;
        List<Map<String, String>> dataMapBatch = new ArrayList<>(batchSize);
        String line;
        int c = 0;
        while ((line = reader.readLine()) != null) {
            Map<String, String> dataMap = new HashMap<>();
            String[] data = parseLine(line, header.length);
            for (int i = 0; i < header.length; i++) {
                dataMap.put(header[i], data[i]);
            }
            dataMap.put(header[0], data[0]);
            dataMapBatch.add(dataMap);
            if (dataMapBatch.size() == batchSize) {
                creator.apply(dataMapBatch);
                dataMapBatch.clear();
            }
            c++;
        }
        creator.apply(dataMapBatch);
        return c;
    }
}
