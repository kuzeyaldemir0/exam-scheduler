package examschd.model;

import java.util.ArrayList;
import java.util.List;

public class Student {
    // Attributes (Private as per UML red square)
    private int studentId;
    private String studentNumber;

    // Relationship: One student has many courses
    private List<Course> enrolledCourses;
    // Relationship: One student has many assignments
    private List<StudentAssignment> assignments;

    // Constructor
    public Student(int studentId, String studentNumber) {
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.enrolledCourses = new ArrayList<>();
        this.assignments = new ArrayList<>();
    }

    // Methods (Public as per UML green circle)
    public List<Course> getEnrolledCourses() {
        return enrolledCourses;
    }

    // Helper method to add a course
    public void enrollInCourse(Course course) {
        if (!enrolledCourses.contains(course)) {
            enrolledCourses.add(course);
        }
    }

    // Getters for other fields
    public int getStudentId() {
        return studentId;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public List<StudentAssignment> getAssignments() {
        return assignments;
    }

    // DAO compatibility methods (aliases for database operations)
    public int getId() {
        return studentId;
    }

    public void setId(int id) {
        this.studentId = id;
    }

    public String getName() {
        return studentNumber;
    }

    public void setName(String name) {
        this.studentNumber = name;
    }
}
