package examschd.model;

import java.time.LocalDateTime;

/**
 * Represents an exam that has been assigned a time slot but not yet assigned specific classrooms.
 * This is an intermediate data structure used by Phase 1 of the scheduling algorithm.
 * Phase 1 produces TimeSlottedExam objects, and Phase 2 consumes them to assign classrooms.
 */
public class TimeSlottedExam {
    private final Course course;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final int durationMinutes;
    private final int studentCount;

    /**
     * Creates a new time-slotted exam.
     *
     * @param course the course being scheduled
     * @param startTime the start time of the exam
     * @param endTime the end time of the exam
     * @param durationMinutes the duration of the exam in minutes
     * @param studentCount the number of students taking this exam
     */
    public TimeSlottedExam(
            Course course,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int durationMinutes,
            int studentCount) {
        this.course = course;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.studentCount = studentCount;
    }

    // Getter methods
    public Course getCourse() {
        return course;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getStudentCount() {
        return studentCount;
    }
}
