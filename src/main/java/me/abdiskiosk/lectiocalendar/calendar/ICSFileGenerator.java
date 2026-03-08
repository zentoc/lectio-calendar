package me.abdiskiosk.lectiocalendar.calendar;

import me.abdiskiosk.lectiocalendar.db.object.LectioEvent;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import net.fortuna.ical4j.validate.ValidationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class ICSFileGenerator {

    public String generateIcs(Collection<? extends LectioEvent> events) throws IOException, ValidationException {
        Calendar calendar = new Calendar();
        calendar.add(new ProdId("-//Lectio Calendar//iCal4j//DK")); //todo: tilføj skoleid
        calendar.add(ImmutableVersion.VERSION_2_0);
        calendar.add(ImmutableCalScale.GREGORIAN);

        for (LectioEvent e : events) {
            DateTime start = new DateTime(e.getStart());
            DateTime end = new DateTime(e.getEnd());

            StringBuilder summary = new StringBuilder();
            if (e.getTeam() != null) {
                summary.append(e.getTeam());
            }
            if (e.getTitle() != null) {
                if (!summary.isEmpty()) {
                    summary.append(" - ");
                }
                summary.append(e.getTitle());
            }
            if (summary.isEmpty()) {
                summary.append("Lectio event");
            }

            VEvent ve = new VEvent(start.toInstant(), end.toInstant(), summary.toString());
            ve.add(new Uid(String.valueOf(summary + "_" + e.getStart().getTime())));

            if (e.getRoom() != null) {
                ve.add(new Location(e.getRoom()));
            }

            StringBuilder desc = new StringBuilder();
            if (e.getTeam() != null) {
                desc.append("Team: ").append(e.getTeam()).append("\\n");
            }
            if (e.getTeachers() != null) {
                desc.append("Teachers: ").append(e.getTeachers()).append("\\n");
            }
            if (e.getStudentHours() != null) {
                desc.append("Student hours: ").append(e.getStudentHours()).append("\\n");
            }
            if (e.getNote() != null && !e.getNote().isBlank()) {
                desc.append("Note: ").append(e.getNote()).append("\\n");
            }
            if (!desc.isEmpty()) {
                ve.add(new Description(desc.toString()));
            }
            if (e.getState() != null) {
                ve.add(new Status(e.getState()));
            }

            calendar.add(ve);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(calendar, baos);
            return baos.toString(StandardCharsets.UTF_8.name());
        }
    }
}