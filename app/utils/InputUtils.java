package utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class InputUtils {
    public static Map<String, String> NOERROR = new HashMap<>();

    public static Integer parseDuration(String durationStrings[]) {
        if (durationStrings == null || durationStrings.length == 0) {
            return null;
        }
        String[] split = durationStrings[0].split(":");
        if (split.length > 4) {
            return null;
        }

        try {
            int duration = 0;
            if (split.length >= 4) {
                duration = Integer.parseInt(split[split.length - 4]);
            }
            if (split.length >= 3) {
                duration = duration * 24 + Integer.parseInt(split[split.length - 3]);
            }
            if (split.length >= 2) {
                duration = duration * 60+ Integer.parseInt(split[split.length - 2]);
            }
            if (split.length >= 1) {
                duration = duration * 60 + Integer.parseInt(split[split.length - 1]);
            }
            return duration;
        } catch (Exception e) {
            // could not parse some integer, too bad
            return null;
        }
    }

    public static String trimToNull(String[] input) {
        if (input == null || input.length == 0) {
            return null;
        }
        return trimToNull(input[0]);
    }

    public static String trimToNull(String input) {
        if (input == null) {
            return null;
        }
        input = input.trim();
        if (input.isEmpty()) {
            return null;
        }
        return input;
    }

    public static Double toDouble(String[] input) {
        if (input == null || input.length == 0) {
            return null;
        }
        return toDouble(input[0]);
    }

    public static Double toDouble(String input) {
        if (input.isEmpty()) {
            return null;
        }
        input = input.trim();
        try {
            return Double.parseDouble(input);
        } catch (Exception e) {
            return null;
        }
    }

    public static Long toLong(String[] input) {
        if (input == null || input.length == 0) {
            return null;
        }
        return toLong(input[0]);
    }

    public static Long toLong(String input) {
        if (input.isEmpty()) {
            return null;
        }
        input = input.trim();
        try {
            return Long.parseLong(input);
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer toInt(String[] input) {
        if (input == null || input.length == 0) {
            return null;
        }
        return toInt(input[0]);
    }

    public static Integer toInt(String input) {
        if (input.isEmpty()) {
            return null;
        }
        input = input.trim();
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
            return null;
        }
    }

    public static void validateString(String input, String name, Map<String, String> errors) {
        validateString(input, name, true, errors);
    }

    public static void validateString(String input, String name, boolean required, Map<String, String> errors) {
        if (required && input == null || input.isBlank()) {
            errors.put(name, ErrorMessages.PLEASE_ENTER_VALUE);
        }
    }

    public static void validateObject(Object object, String name, boolean required, Map<String, String> errors) {
        if (required && object == null) {
            errors.put(name, ErrorMessages.PLEASE_ENTER_VALUE);
        }
    }

    public static void validateUrl(String url, String name, boolean required, Map<String, String> errors) {
        if (!required && url.isBlank()) {
            return;
        }
        try {
            new URL(url);
        } catch (Exception e) {
            errors.put(name, ErrorMessages.PLEASE_ENTER_VALID_URL);
        }
    }

    public static void validateInt(Integer integer, String name, boolean required, Integer minValue, Integer maxValue, Map<String, String> errors) {
        if (integer == null) {
            if (required) {
                errors.put(name, ErrorMessages.PLEASE_ENTER_VALUE);
            }
            return;
        }
        if (minValue != null && integer < minValue) {
            errors.put(name, ErrorMessages.MIN_VALUE_IS("" + minValue));
            return;
        }
        if (maxValue != null && integer > maxValue) {
            errors.put(name, ErrorMessages.MAX_VALUE_IS("" + maxValue));
            return;
        }
    }
}
