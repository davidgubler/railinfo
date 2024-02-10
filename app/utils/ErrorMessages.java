package utils;

public class ErrorMessages {
    public static String PLEASE_ENTER_VALUE = "Please enter a value";
    public static String PLEASE_ENTER_VALID_URL = "Please enter a valid URL";
    public static String PLEASE_ENTER_VALID_COORDINATES = "Please enter valid coordinates";
    public static String PLEASE_ENTER_VALID_DATABASE_NAME = "Please enter a valid database name (must start with \"railinfo-\")";
    public static String PLEASE_ENTER_DIFFERENT_NAME = "This name is already in use. Please use a different name.";
    public static String EMAIL_OR_PASSWORD_INVALID = "Email Address or Password invalid";
    public static String EDGE_NOT_FOUND = "Edge not found";
    public static String STOP_NOT_FOUND = "Stop not found";
    public static String MIN_VALUE_IS(String minValue) {
        return "Minimum value is " + minValue;
    }
    public static String MAX_VALUE_IS(String maxValue) {
        return "Maximum value is " + maxValue;
    }
}
