package examschd.model;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private int courseId;
    private String courseCode;

    // Relationship: One course has many students
    private List<Student> enrolledStudents;
    // Relationship: One course has many exam sessions
    private List<ExamSession> examSessions;

    public Course(int courseId, String courseCode) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.enrolledStudents = new ArrayList<>();
        this.examSessions = new ArrayList<>();
    }

    public List<Student> getStudents() {
        return enrolledStudents;
    }

    // Helper to add student
    public void addStudent(Student student) {
        if (!enrolledStudents.contains(student)) {
            enrolledStudents.add(student);
        }
    }

    public int getCourseId() { 
        return courseId; 
    }
    
    public String getCourseCode() { 
        return courseCode; 
    }

    public List<ExamSession> getExamSessions() { 
        return examSessions; 
    }
}