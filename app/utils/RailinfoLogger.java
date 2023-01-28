package utils;

import play.mvc.Http;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RailinfoLogger {
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-mm-dd HH:mm:ss");

    public static void info(Http.RequestHeader request, String message) {
        String callerClass = Thread.currentThread().getStackTrace()[2].getClassName();
        System.out.println(dateTimeFormatter.format(LocalDateTime.now()) + " " + request.remoteAddress() + " " + callerClass + " " + message);
    }
}
