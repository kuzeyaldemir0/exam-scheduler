package examschd.model;

import java.util.ArrayList;
import java.util.List;

public class Student
{
    private int studentId;
    private String studentNumber;

    // CHANGED: Now holds Enrollments instead of Courses directly
    private List<Enrollment> enrollments;
    private List<StudentAssignment> assignments;

    public Student(int studentId, String studentNumber)
    {
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.enrollments = new ArrayList<>();
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

    // Helper method: If you still need a list of Courses for your Algorithm
    public List<Course> getEnrolledCourses()
    {
        List<Course> courses = new ArrayList<>();
        for (Enrollment e : enrollments)
        {
            courses.add(e.getCourse());
        }
        return courses;
    }

    public int getStudentId() { return studentId; }
    public String getStudentNumber() { return studentNumber; }
    public List<StudentAssignment> getAssignments() { return assignments; }
}