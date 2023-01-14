package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
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

    private static DateTimeFormatter hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static DateTimeFormatter hourMinuteSecondFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String formatStopTime(LocalDate startDate, LocalDateTime time) {
        if (time == null) {
            return "";
        }
        Period period = Period.between(startDate, time.toLocalDate());
        return hourMinuteFormatter.format(time) + (period.isZero() ? "" : "+" + period.getDays());
    }

    public static String formatWaypointTime(LocalDate startDate, LocalDateTime time) {
        if (time == null) {
            return "";
        }
        Period period = Period.between(startDate, time.toLocalDate());
        return hourMinuteSecondFormatter.format(time) + (period.isZero() ? "" : "+" + period.getDays());
    }
}
