package controllers;

import entities.Stop;
import play.mvc.*;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ImportController extends Controller {

    private List<String> parseLine(String line) {
        List<String> components = new LinkedList<>();
        while (!line.isBlank()) {
            if (line.startsWith("\"")) {
                int endPos = line.indexOf("\"", 1);
                if (endPos == -1) {
                    endPos = line.length();
                }

                components.add(line.substring(1, endPos));
                line = line.substring(Math.min(endPos + 1, line.length()));
            } else {
                int endPos = line.indexOf(",");
                if (endPos == -1) {
                    endPos = line.length();
                }
                components.add(line.substring(0, endPos));
                line = line.substring(Math.min(endPos, line.length()));
            }
            if (line.startsWith(",")) {
                line = line.substring(1);
            }
        }
        return components;
    }

    public Result flubber() throws IOException {
        List<Stop> stops = new LinkedList<>();

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
            String line;
            if ("stops.txt".equals(entry.getName())) {
                reader.readLine(); // ignore first line
                while((line = reader.readLine())!= null){
                    try {
                        stops.add(new Stop(parseLine(line)));
                    } catch ( Exception e ) {
                        System.out.println("could not parse line: " + line);
                    }
                }
            } else {
                while((line = reader.readLine())!= null) {
                    // just skip it
                }
            }

            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();

        System.out.println("found " + stops.size() + " stops");

        return ok();
    }
}
