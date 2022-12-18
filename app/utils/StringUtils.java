package utils;

import java.util.List;

public class StringUtils {
    public static String join(List<? extends Object> objectList, String separator) {
        if (objectList == null || objectList.isEmpty()) {
            return "";
        }
        String string = objectList.get(0).toString();
        for (int i = 1; i < objectList.size(); i++) {
            string += separator;
            string += objectList.get(i);
        }
        return string;
    }

    public static String formatSeconds(int totalSeconds) {
        int hours = totalSeconds/3600;
        int minutes = (totalSeconds - hours*60) / 60;
        int seconds = totalSeconds % 60;
        String s = String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
        if (hours > 0) {
            s = hours + ":" + s;
        }
        return s;
    }
}
