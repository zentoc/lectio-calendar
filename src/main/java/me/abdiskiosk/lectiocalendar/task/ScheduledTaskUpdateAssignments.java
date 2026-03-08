package me.abdiskiosk.lectiocalendar.task;

import lombok.NonNull;
import me.abdiskiosk.lectiocalendar.db.object.LectioAssignmentEvent;
import me.abdiskiosk.lectiocalendar.lectio.LectioClient;
import me.abdiskiosk.lectiocalendar.lectio.LectioWindow;

import java.util.Collection;


public class ScheduledTaskUpdateAssignments implements Runnable {

    private final LectioClient lectioClient;

    public ScheduledTaskUpdateAssignments(@NonNull LectioClient lectioClient) {
        this.lectioClient = lectioClient;
    }

    @Override
    public void run() {

        LectioWindow window = lectioClient.openWindow();
        try {
            Collection<LectioAssignmentEvent> assignments = window.getAssignments();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                window.close();
            } catch (Exception ignored) {
            }
        }
    }
}
