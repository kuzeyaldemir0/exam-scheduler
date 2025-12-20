package examschd.model;

/**
 * Represents the reason why a course could not be scheduled.
 * Used to provide actionable feedback to users.
 */
public enum SchedulingFailureReason {
    STUDENT_CONFLICT(
        "Student scheduling conflicts",
        "Students in this course have too many overlapping exams. Try extending the date range"
    ),
    MAX_EXAMS_PER_DAY_EXCEEDED(
        "Daily exam limit reached",
        "Increase 'Max exams per student per day' in Filter Settings or extend the date range"
    ),
    CLASSROOM_CAPACITY_INSUFFICIENT(
        "Not enough classroom capacity",
        "Add more classrooms or extend the date range to spread exams across more days"
    ),
    NO_AVAILABLE_SLOTS(
        "No available time slots",
        "Extend the date range to provide more scheduling options"
    );

    private final String displayMessage;
    private final String suggestion;

    SchedulingFailureReason(String displayMessage, String suggestion) {
        this.displayMessage = displayMessage;
        this.suggestion = suggestion;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public String getSuggestion() {
        return suggestion;
    }
}
