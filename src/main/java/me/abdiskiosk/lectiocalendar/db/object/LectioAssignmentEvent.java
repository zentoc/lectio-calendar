package me.abdiskiosk.lectiocalendar.db.object;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@Data
public class LectioAssignmentEvent implements LectioEvent {
    private final @Nullable String title;
    private final @Nullable String note;
    private final @Nullable String team;
    private final @Nullable Double studentHours;
    private final Date start;
    private final Date end;

    public LectioAssignmentEvent(String title, String note, String team, Double studentHours, Date deadline) {
        this.title = title;
        this.note = note;
        this.team = team;
        this.studentHours = studentHours;
        this.start = new Date(deadline.getTime() - 60 * 60 * 1000);
        this.end = deadline;
    }
}
