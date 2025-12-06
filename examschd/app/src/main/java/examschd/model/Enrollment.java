package examschd.model;

public class Enrollment
{
    private int enrollmentId;

    // The link to the Student
    private Student student;

    // The link to the Course
    private Course course;

    public Enrollment(int enrollmentId, Student student, Course course)
    {
        this.enrollmentId = enrollmentId;
        this.student = student;
        this.course = course;
    }

    public int getEnrollmentId() { return enrollmentId; }
    public Student getStudent() { return student; }
    public Course getCourse() { return course; }
}