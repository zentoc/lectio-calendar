package me.abdiskiosk.lectiocalendar.db.object;
import lombok.Data;

import java.util.Date;

@Data
public class LectioAssignment {
    private final String title;
    private final String note;
    private final String team;
    private final Double StudentHours;
    private final Date deadline;
}
