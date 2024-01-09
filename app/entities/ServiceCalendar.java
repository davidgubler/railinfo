package entities;

import java.time.LocalDate;

public interface ServiceCalendar {
    String getServiceId();

    boolean getMonday();

    boolean getTuesday();

    boolean getWednesday();

    boolean getThursday();

    boolean getFriday();

    boolean getSaturday();

    boolean getSunday();

    LocalDate getStart();

    LocalDate getEnd();
}
