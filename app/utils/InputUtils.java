package utils;

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
}
