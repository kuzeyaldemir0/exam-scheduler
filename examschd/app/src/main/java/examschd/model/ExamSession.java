package examschd.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExamSession
{
    private int sessionId;
    private Date examDate;
    private String timeSlot;
    private int durationMinutes;

    // Relationship: Course "has" ExamSession
    private Course course;

    // Relationship: Composition (Diamond) - Session splits into Partitions
    private List<ExamPartition> partitions;

    public ExamSession(int sessionId, Date examDate, String timeSlot, int durationMinutes, Course course)
    {
        this.sessionId = sessionId;
        this.examDate = examDate;
        this.timeSlot = timeSlot;
        this.durationMinutes = durationMinutes;
        this.course = course;
        this.partitions = new ArrayList<>();
    }

    public void addPartition(ExamPartition partition) {
        this.partitions.add(partition);
    }

    public List<ExamPartition> getPartitions() { return partitions; }
    public int getSessionId() { return sessionId; }
    public Date getExamDate() { return examDate; }
    public String getTimeSlot() { return timeSlot; }
    public Course getCourse() { return course; }
}