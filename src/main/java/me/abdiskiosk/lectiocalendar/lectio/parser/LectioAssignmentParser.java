package me.abdiskiosk.lectiocalendar.lectio.parser;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import me.abdiskiosk.lectiocalendar.db.object.LectioAssignment;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// den her blev ramt af chat
public class LectioAssignmentParser {

    private static final String ASSIGNMENTS_TABLE_SELECTOR = "table#s_m_Content_Content_ExerciseGV";

    /**
     * Parses assignments from the assignments page.
     *
     * We explicitly disable "Vis kun aktuelle" so we get the full history (otherwise
     * Lectio often limits the table to only current/relevant assignments).
     */
    public @NotNull Collection<LectioAssignment> parseAssignments(@NotNull Page page) {
        ensureOnlyCurrentUnchecked(page);

        // Wait for the table to be present.
        try {
            page.waitForSelector(ASSIGNMENTS_TABLE_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(4000));
            // Give Lectio a moment to finish any partial updates.
            page.waitForTimeout(250);
        } catch (PlaywrightException e) {
            return List.of();
        }

        ElementHandle table = page.querySelector(ASSIGNMENTS_TABLE_SELECTOR);
        if (table == null) return List.of();

        // Re-query rows after any filtering. Don't keep references from before DOM refresh.
        List<ElementHandle> rows = table.querySelectorAll("tbody > tr");
        List<LectioAssignment> assignments = new ArrayList<>();

        for (ElementHandle row : rows) {
            if (!row.querySelectorAll("th").isEmpty()) continue;

            List<ElementHandle> tds = row.querySelectorAll("td");
            if (tds.size() < 9) continue;

            String team = text(tds, 1);
            String title = textFromLinkOrCell(tds.get(2));
            Date deadline = parseDeadline(text(tds, 3));
            Double studentHours = parseDanishDouble(text(tds, 4));
            String note = text(tds, 8);

            // If the row is somehow empty, skip
            if ((title == null || title.isBlank()) && (team == null || team.isBlank())) continue;

            assignments.add(new LectioAssignment(title, note, team, studentHours, deadline));
        }

        return assignments;
    }

    private String text(@NotNull List<ElementHandle> tds, int index) {
        if (index < 0 || index >= tds.size()) return null;
        return nullIfBlank(tds.get(index).innerText());
    }

    private String textFromLinkOrCell(@NotNull ElementHandle cell) {
        ElementHandle link = cell.querySelector("a");
        if (link != null) return nullIfBlank(link.innerText());
        return nullIfBlank(cell.innerText());
    }

    private Date parseDeadline(String raw) {
        raw = safe(raw);
        if (raw.isBlank()) return null;

        // Example: "18/8-2025 16:00" (single-digit day/month)
        // Note: SimpleDateFormat doesn't handle "d/M-yyyy" with '-' before year unless pattern includes it.
        String[] patterns = new String[]{
                "d/M-yyyy HH:mm",
                "dd/MM-yyyy HH:mm",
                "d/M-yyyy",
                "dd/MM-yyyy"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat(pattern, Locale.forLanguageTag("da-DK"));
                fmt.setLenient(false);
                return fmt.parse(raw.trim());
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private Double parseDanishDouble(String raw) {
        raw = safe(raw).trim();
        if (raw.isBlank()) return null;
        // Example: "1,50" or "0,00"
        raw = raw.replace("\u00A0", "");
        raw = raw.replace(".", "");
        raw = raw.replace(",", ".");
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String nullIfBlank(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

    private void ensureOnlyCurrentUnchecked(@NotNull Page page) {
        // Wait a bit for the filter area to exist.
        try {
            page.waitForSelector("text=Vis kun aktuelle", new Page.WaitForSelectorOptions().setTimeout(1500));
        } catch (PlaywrightException e) {
            // Page variant without this filter; nothing to do.
            return;
        }

        ElementHandle checkbox = findOnlyCurrentCheckbox(page);
        if (checkbox == null) return;

        try {
            if (checkbox.isChecked()) {
                // Don't rely on row count changing; sometimes it doesn't change immediately.
                checkbox.uncheck();

                // Wait until checkbox state is applied and table exists.
                page.waitForFunction(
                        "([sel]) => { const cb = document.querySelector(sel); return cb && !cb.checked; }",
                        new Object[]{"input[type='checkbox']"},
                        new Page.WaitForFunctionOptions().setTimeout(2000)
                );

                // Wait for the assignments table to refresh/settle.
                page.waitForSelector(ASSIGNMENTS_TABLE_SELECTOR + " tbody > tr", new Page.WaitForSelectorOptions().setTimeout(5000));
                page.waitForTimeout(500);
            }
        } catch (PlaywrightException ignored) {
        }
    }

    private ElementHandle findOnlyCurrentCheckbox(@NotNull Page page) {
        // 1) Preferred: a <label for="id">Vis kun aktuelle</label> -> input#id
        ElementHandle label = page.querySelector("label:has-text('Vis kun aktuelle')");
        if (label != null) {
            String forId = label.getAttribute("for");
            if (forId != null && !forId.isBlank()) {
                ElementHandle input = page.querySelector("#" + cssEscapeId(forId));
                if (input != null) return input;
            }
        }

        // 2) Next: label wraps the input: <label><input type=checkbox>Vis kun aktuelle</label>
        if (label != null) {
            ElementHandle wrappedInput = label.querySelector("input[type='checkbox']");
            if (wrappedInput != null) return wrappedInput;
        }

        // 3) Fallback: find the nearest checkbox in the same container.
        return page.querySelector(
                ":is(label,span,div):has-text('Vis kun aktuelle')"
                        + " >> xpath=ancestor::*[self::span or self::div or self::td][1]"
                        + "//input[@type='checkbox'][1]"
        );
    }

    private String cssEscapeId(@NotNull String id) {
        return id.replace(":", "\\:");
    }
}
