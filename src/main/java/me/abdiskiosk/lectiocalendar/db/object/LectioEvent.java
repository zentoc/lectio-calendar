package me.abdiskiosk.lectiocalendar.db.object;

import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * Base interface for anything that can be exported as a calendar event.
 *
 * Concrete implementations can add extra fields. Common optional fields are exposed here
 * as default methods so implementations only need to provide what they actually have.
 */
public interface LectioEvent {

    @Nullable String getTitle();

    Date getStart();

    Date getEnd();

    /** Optional team/class. */
    default @Nullable String getTeam() { return null; }

    /** Optional free-text note/description. */
    default @Nullable String getNote() { return null; }

    /** Optional student hours (assignments). */
    default @Nullable Double getStudentHours() { return null; }

    /** Optional room (schedule). */
    default @Nullable String getRoom() { return null; }

    /** Optional teachers (schedule). */
    default @Nullable String getTeachers() { return null; }

    /** Optional state/status (schedule). */
    default @Nullable String getState() { return null; }
}
