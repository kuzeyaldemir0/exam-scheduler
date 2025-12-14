package examschd.model;

import java.util.ArrayList;
import java.util.List;

public class Student 
{
    // Attributes (Private as per UML red square)
    private int studentId;
    private String studentName;

    // CHANGED: Now holds Enrollments instead of Courses directly
    private List<Enrollment> enrollments;
    private List<StudentAssignment> assignments;

    // Constructor
    public Student(int studentId, String studentName) 
    {
        this.studentId = studentId;
        this.studentName = studentName;
        this.enrolledCourses = new ArrayList<>();
        this.assignments = new ArrayList<>();
    }

    public void addEnrollment(Enrollment enrollment)
    {
        this.enrollments.add(enrollment);
    }

    public List<Enrollment> getEnrollments()
    {
        return enrollments;
    }

    // Helper method to add a course
    public void enrollInCourse(Course course) 
    {
        if (!enrolledCourses.contains(course)) 
        {
            enrolledCourses.add(course);
        }
        return courses;
    }

    // Getters for other fields
    public int getStudentId() 
    {
        return studentId;
    }

    public String getStudentName() 
    {
        return studentName;
    }

    public List<StudentAssignment> getAssignments() 
    {
        return assignments;
    }

    // DAO compatibility methods (aliases for database operations)
    public int getId() 
    {
        return studentId;
    }

    public void setId(int id) 
    {
        this.studentId = id;
    }
}
