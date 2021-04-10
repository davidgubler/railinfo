package controllers;

import entities.ServiceCalendar;
import entities.Stop;
import entities.StopTime;
import entities.Trip;
import play.mvc.*;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ImportController extends Controller {

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
        Map<String, String> dataMap = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] data = parseLine(line, header.length);
            for (int i = 0; i < header.length; i++) {
                dataMap.put(header[i], data[i]);
            }
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
                stops = parseFile(zipIn, dataMap -> new Stop(dataMap));
            } else if ("trips.txt".equals(entry.getName())) {
                trips = parseFile(zipIn, dataMap -> new Trip(dataMap));
            } else if ("calendar.txt".equals(entry.getName())) {
                serviceCalendars = parseFile(zipIn, dataMap -> new ServiceCalendar(dataMap));
            } else if ("stop_times.txt".equals(entry.getName())) {
                stopTimes = parseFile(zipIn, dataMap -> new StopTime(dataMap));
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
}
