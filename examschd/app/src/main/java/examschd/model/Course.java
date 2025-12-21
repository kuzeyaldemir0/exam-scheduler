package examschd.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Course {
    private int courseId;
    private String courseName;

    // Relationship: One course has many students
    private List<Student> enrolledStudents;
    // Relationship: One course has many exam sessions
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
    }

    public List<ExamSession> getExamSessions() {
        return examSessions;
    }

    private int durationMinutes = 120;

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return courseId == course.courseId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId);
    }

    @Override
    public String toString() {
        return courseName;
    }
}