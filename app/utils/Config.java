package utils;

public class Config {
    public static final String GLOBAL_DB = "railinfo";
    public static final String TIMETABLE_DB = "railinfo-ch";

    public enum Option { MAPS_KEY }

    public static String get(Option o) {
        return System.getenv().get(o.name());
    }
}
