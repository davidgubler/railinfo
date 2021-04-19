package controllers;

import com.google.inject.Inject;
import entities.ServiceCalendar;
import entities.Stop;
import entities.StopTime;
import entities.Trip;
import models.ServiceCalendarsModel;
import models.StopTimesModel;
import models.StopsModel;
import models.TripsModel;
import play.mvc.*;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ImportController extends Controller {

    public static final String UTF8_BOM = "\uFEFF";

    @Inject
    private StopsModel stopsModel;

    @Inject
    private StopTimesModel stopTimesModel;

    @Inject
    private TripsModel tripsModel;

    @Inject
    private ServiceCalendarsModel serviceCalendarsModel;

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

    private <T> List<T> parseFile(InputStream is, Function<Map<String, String>, T> creator) throws IOException {
        List<T> list = new LinkedList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String[] header = parseLine(reader.readLine(), 100);
        if (header[0].startsWith(UTF8_BOM)) {
            // nobody likes a byte order mark
            header[0] = header[0].substring(1);
        }
        Map<String, String> dataMap = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] data = parseLine(line, header.length);
            for (int i = 0; i < header.length; i++) {
                dataMap.put(header[i], data[i]);
            }
            dataMap.put(header[0], data[0]);
            list.add(creator.apply(dataMap));
        }
        return list;
    }

    private List<Stop> stops = new LinkedList<>();
    private List<Trip> trips = new LinkedList<>();
    private List<ServiceCalendar> serviceCalendars = new LinkedList<>();
    private List<StopTime> stopTimes = new LinkedList<>();

    public Result flubber() throws IOException {
        long start = System.currentTimeMillis();
        File timetableZip = new File("/home/david/Downloads/gtfs_fp2021_2021-04-07_09-10.zip");
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(timetableZip));

        ZipEntry entry = zipIn.getNextEntry();

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
            } else if ("calendar.txt".equals(entry.getName())) {
                serviceCalendarsModel.drop();
                serviceCalendars = parseFile(zipIn, dataMap -> serviceCalendarsModel.create(dataMap));
            } else if ("stop_times.txt".equals(entry.getName())) {
                stopTimesModel.drop();
                stopTimes = parseFile(zipIn, dataMap -> stopTimesModel.create(dataMap));
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

        System.out.println("found " + stops.size() + " stops");
        System.out.println("found " + trips.size() + " trips");
        System.out.println("found " + serviceCalendars.size() + " serviceCalendars");
        System.out.println("found " + stopTimes.size() + " stopTimes");
        System.out.println("time taken: " + (System.currentTimeMillis() - start) + " ms");
        return ok();
    }

    public Result effretikon()  {
        String id = null;
        for (Stop stop : stops) {
            if ("Effretikon".equals(stop.getName())) {
                id = stop.getStopId().split(":")[0];
                break;
            }
        }

        System.out.println("Effretikon has ID " + id);

        String finalId = id;
        List<StopTime> stopTimesEffretikon = stopTimes.stream().filter(st -> st.getStopId().startsWith(finalId + ":")).collect(Collectors.toList());
        Set<String> tripIdsEffretikon = stopTimesEffretikon.stream().map(StopTime::getTripId).collect(Collectors.toSet());
        System.out.println("found " + tripIdsEffretikon.size() + " trip ids");

        List<Trip> tripsEffretikon = trips.stream().filter(trip -> tripIdsEffretikon.contains(trip.getTripId())).collect(Collectors.toList());
        System.out.println("found " + tripsEffretikon.size() + " trips");

        Map<String, ServiceCalendar> calendarLookup = serviceCalendars.stream().collect(Collectors.toMap(ServiceCalendar::getServiceId, Function.identity()));
        List<Trip> tripsEffretikonActive = tripsEffretikon.stream().filter(t -> calendarLookup.get(t.getServiceId()).isActiveToday()).collect(Collectors.toList());
        System.out.println("found " + tripsEffretikonActive.size() + " trips for today");

        for (Trip trip : tripsEffretikonActive) {
            System.out.println(trip.getTripShortName() + " : " + trip.getTripHeadsign());
        }

        return ok();
    }
}
