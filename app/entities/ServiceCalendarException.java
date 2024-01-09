package entities;

import java.time.LocalDate;

public interface ServiceCalendarException {
    String getServiceId();

    LocalDate getDate();

    boolean getActive();
}
