package me.abdiskiosk.lectiocalendar.lectio;

import dk.zentoc.LectioSession;
import me.abdiskiosk.lectiocalendar.db.object.LectioAssignmentEvent;
import me.abdiskiosk.lectiocalendar.db.object.LectioCalendarEvent;
import me.abdiskiosk.lectiocalendar.lectio.parser.LectioAssignmentParser;
import me.abdiskiosk.lectiocalendar.lectio.parser.LectioScheduleParser;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class LectioWindow {

    private final String schoolId;
    private final LectioSession session;

    public LectioWindow(@NotNull String schoolId, @NotNull LectioSession session) {
        this.schoolId = schoolId;
        this.session = session;
    }

    public @NotNull Collection<LectioCalendarEvent> getEvents(int year, int weekNum) throws Exception {
        System.out.println("LINK: " + generateScheduleUrl(year, weekNum));
        session.page().navigate(generateScheduleUrl(year, weekNum));

        session.page().waitForLoadState();
        System.out.println("loaded");

        try {
            return new LectioScheduleParser().parseSchedule(session.page(), weekNum, year);
        } catch (Exception e) {
            System.err.println("Error parsing schedule: " + e.getMessage());
            throw new Exception(e);
        }
    }

    public @NotNull Collection<LectioAssignmentEvent> getAssignments() throws Exception {
        session.page().navigate(generateAssignmentUrl());
        session.page().waitForLoadState();
        try {
            return new LectioAssignmentParser().parseAssignments(session.page());
        } catch (Exception e) {
            System.err.println("Error parsing assignments: " + e.getMessage());
            throw new Exception(e);
        }
    }


    @SuppressWarnings("deprecation")
    protected String generateScheduleUrl(int year, int weekNum) {
        String weekString = String.valueOf(weekNum);
        if(weekString.length() == 1) {
            weekString = "0" + weekString;
        }
        weekString += year;
        return String.format("https://www.lectio.dk/lectio/%s/SkemaNy.aspx?showtype=0&week=%s", schoolId, weekString);
    }

    protected String generateAssignmentUrl() {
        return String.format("https://www.lectio.dk/lectio/%s/OpgaverElev.aspx", schoolId);
    }


    public void close() {
        session.close();
    }

}
