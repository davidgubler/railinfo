package utils;

public class Config {
    public static final String GLOBAL_DB = "railinfo";

    public enum Option {
        MAPS_KEY, //
        TLS_CERT_CHAIN, //
        TLS_CERT_CHAIN_FILE, //
        TLS_PRIVATE_KEY, //
        TLS_PRIVATE_KEY_FILE;

        public String get() {
            return System.getenv().get(this.name());
        }
    }

    public static String get(Option o) {
        return System.getenv().get(o.name());
    }
}
