package examschd.model;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private int courseId;
    private String courseName;

    // CHANGED: Now holds Enrollments instead of Students directly
    private List<Enrollment> enrollments;
    private List<ExamSession> examSessions;

    // Constructor
    public Course(int courseId, String courseName) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.enrolledStudents = new ArrayList<>();
        this.examSessions = new ArrayList<>();
    }

    // Getter & Setter
    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    // Relationship methods
    public List<Student> getStudents() {
        return enrolledStudents;
    }

    public void addStudent(Student student) {
        if (!enrolledStudents.contains(student)) {
            enrolledStudents.add(student);
        }
        return students;
    }

    public List<ExamSession> getExamSessions() {
        return examSessions;
    }

    private int durationMinutes = 120;

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

}