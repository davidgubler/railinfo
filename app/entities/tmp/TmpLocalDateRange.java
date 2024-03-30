package entities.tmp;

import entities.LocalDateRange;

import java.time.LocalDate;

public class TmpLocalDateRange implements LocalDateRange {
    private LocalDate start;

    private LocalDate end;

    public TmpLocalDateRange(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return getStart() + " - " + getEnd();
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }
}
