package utils;

public class Config {
    public enum Option { MAPS_KEY }

    public static String get(Option o) {
        return System.getenv().get(o.name());
    }
}
