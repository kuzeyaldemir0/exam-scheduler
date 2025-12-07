package examschd.model;

import java.util.ArrayList;
import java.util.List;

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

    // ENROLLMENT TODO #1 DONE
    private int examDuration; // in minutes

    public Course(int id, String courseName, int examDuration) {
        this.id = id;
        this.courseName = courseName;
        this.examDuration = examDuration;
        this.students = new ArrayList<>();
        this.examSessions = new ArrayList<>();
    }

    public int getExamDuration() {
        return examDuration;
    }

    public void setExamDuration(int examDuration) {
        this.examDuration = examDuration;
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
}