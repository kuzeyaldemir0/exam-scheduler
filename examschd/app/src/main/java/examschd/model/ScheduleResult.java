package examschd.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Wrapper class to hold both scheduled exams and courses that couldn't be scheduled.
 */
public class ScheduleResult {
    private final Map<LocalDate, List<ExamSession>> schedule;
    private final List<Course> unscheduledCourses;

    public ScheduleResult(Map<LocalDate, List<ExamSession>> schedule, List<Course> unscheduledCourses) {
        this.schedule = schedule;
        this.unscheduledCourses = unscheduledCourses;
    }

    public Map<LocalDate, List<ExamSession>> getSchedule() {
        return schedule;
    }

    public List<Course> getUnscheduledCourses() {
        return unscheduledCourses;
    }
}
