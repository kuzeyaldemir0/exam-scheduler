package examschd.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExamSession {
    private int sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int durationMinutes;

    // Relationship: Course "has" ExamSession
    private Course course;

    // Relationship: Composition (Diamond) - Session splits into Partitions
    private List<ExamPartition> partitions;

    public ExamSession(int sessionId, LocalDateTime startTime, LocalDateTime endTime, int durationMinutes, Course course) {
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.course = course;
        this.partitions = new ArrayList<>();
    }

    // Legacy constructor for backward compatibility
    @Deprecated
    public ExamSession(int sessionId, Date examDate, String timeSlot, int durationMinutes, Course course) {
        this.sessionId = sessionId;
        this.startTime = null;
        this.endTime = null;
        this.durationMinutes = durationMinutes;
        this.course = course;
        this.partitions = new ArrayList<>();
    }

    public void addPartition(ExamPartition partition) {
        this.partitions.add(partition);
    }

    public List<ExamPartition> getPartitions() {
        return partitions;
    }

    public int getSessionId() {
        return sessionId;
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

    public String getTimeSlot() {
        if (startTime == null || endTime == null) return "Unknown";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return startTime.format(formatter) + "-" + endTime.format(formatter);
    }

    public Course getCourse() {
        return course;
    }

    public boolean overlaps(ExamSession other, int breakTimeMinutes) {
        if (other == null || other.startTime == null || other.endTime == null ||
            this.startTime == null || this.endTime == null) {
            return false;
        }
        return !(this.endTime.plusMinutes(breakTimeMinutes).isBefore(other.startTime) ||
                 other.endTime.plusMinutes(breakTimeMinutes).isBefore(this.startTime));
    }
}