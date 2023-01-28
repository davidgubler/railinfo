package controllers;

import com.google.inject.Inject;
import entities.User;
import models.*;
import play.mvc.*;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DataImportController extends Controller {

    public static final String UTF8_BOM = "\uFEFF";

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
    private UsersModel usersModel;

    public Result index(Http.Request request) {
        User user = usersModel.getFromRequest(request);
        return ok(views.html.dataimport.index.render(request, user));
    }

    public Result importGtfsPost(Http.Request request) {
        new Thread(() -> {
            try {
                importGtfs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        return ok();
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
        int batchSize = 10;
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



    private void importGtfs() throws IOException {
        long start = System.currentTimeMillis();
        File timetableZip = new File("/home/david/Downloads/gtfs_fp2023_2022-12-14_04-15.zip");
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(timetableZip));

        ZipEntry entry = zipIn.getNextEntry();

        int stops = 0, trips = 0, routes = 0, stopTimes = 0, serviceCalendars = 0, serviceCalendarExceptions = 0;

        while (entry != null) {
            if (entry.isDirectory()) {
                continue;
            }
            System.out.println(entry.getName());

            InputStreamReader zipInReader = new InputStreamReader(zipIn);
            BufferedReader reader = new BufferedReader(zipInReader);

            if ("stops.txt".equals(entry.getName())) {
                stopsModel.drop();
                stops = parseFile(zipIn, dataMap -> stopsModel.create(dataMap));
            } else if ("trips.txt".equals(entry.getName())) {
                tripsModel.drop();
                trips = parseFile(zipIn, dataMap -> tripsModel.create(dataMap));
            } else if ("routes.txt".equals(entry.getName())) {
                routesModel.drop();
                routes = parseFile(zipIn, dataMap -> routesModel.create(dataMap));
            } else if ("stop_times.txt".equals(entry.getName())) {
                stopTimesModel.drop();
                stopTimes = parseFile(zipIn, dataMap -> stopTimesModel.create(dataMap));
            } else if ("calendar.txt".equals(entry.getName())) {
                serviceCalendarsModel.drop();
                serviceCalendars = parseFile(zipIn, dataMap -> serviceCalendarsModel.create(dataMap));
            } else if ("calendar_dates.txt".equals(entry.getName())) {
                serviceCalendarExceptionsModel.drop();
                serviceCalendarExceptions = parseFile(zipIn, dataMap -> serviceCalendarExceptionsModel.create(dataMap));
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

        System.out.println("found " + stops + " stops");
        System.out.println("found " + trips + " trips");
        System.out.println("found " + routes + " routes");
        System.out.println("found " + stopTimes + " stopTimes");
        System.out.println("found " + serviceCalendars + " serviceCalendars");
        System.out.println("found " + serviceCalendarExceptions + " serviceCalendarExceptions");
        System.out.println("time taken: " + (System.currentTimeMillis() - start) + " ms");
    }
}
