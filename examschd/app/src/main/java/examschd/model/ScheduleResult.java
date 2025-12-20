package examschd.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper class to hold both scheduled exams and courses that couldn't be scheduled.
 */
public class ScheduleResult {
    private final Map<LocalDate, List<ExamSession>> schedule;
    private final List<Course> unscheduledCourses;
    private final Map<Course, SchedulingFailureReason> failureReasons;

    public ScheduleResult(Map<LocalDate, List<ExamSession>> schedule, List<Course> unscheduledCourses) {
        this(schedule, unscheduledCourses, new HashMap<>());
    }

    public ScheduleResult(Map<LocalDate, List<ExamSession>> schedule,
                          List<Course> unscheduledCourses,
                          Map<Course, SchedulingFailureReason> failureReasons) {
        this.schedule = schedule;
        this.unscheduledCourses = unscheduledCourses;
        this.failureReasons = failureReasons;
    }

    public Map<LocalDate, List<ExamSession>> getSchedule() {
        return schedule;
    }

    public List<Course> getUnscheduledCourses() {
        return unscheduledCourses;
    }

    public Map<Course, SchedulingFailureReason> getFailureReasons() {
        return failureReasons;
    }

    public SchedulingFailureReason getFailureReason(Course course) {
        return failureReasons.get(course);
    }
}
