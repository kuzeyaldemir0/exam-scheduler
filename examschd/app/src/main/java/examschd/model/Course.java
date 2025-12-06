package examschd.model;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private int courseId;
    private String courseCode;

    // CHANGED: Now holds Enrollments instead of Students directly
    private List<Enrollment> enrollments;
    private List<ExamSession> examSessions;

    public Course(int courseId, String courseCode) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.enrollments = new ArrayList<>();
        this.examSessions = new ArrayList<>();
    }

    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    // Helper method for your Algorithm
    public List<Student> getEnrolledStudents() {
        List<Student> students = new ArrayList<>();
        for (Enrollment e : enrollments) {
            students.add(e.getStudent());
        }
        return students;
    }

    public int getCourseId() { return courseId; }
    public String getCourseCode() { return courseCode; }
    public List<ExamSession> getExamSessions() { return examSessions; }
}