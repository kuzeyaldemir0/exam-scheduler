package examschd.model;

import java.util.List;

public class Enrollment {
    private int enrollmentId;
    private int[] studentIds;
    private String courseName;

    public Enrollment(int enrollmentId, int[] studentIds, String courseName) {
        this.enrollmentId = enrollmentId;
        this.studentIds = studentIds;
        this.courseName = courseName;
    }

    // CSVReader için geçici constructor
    private List<String> studentNumbers; // CSV’den gelen ham veri
    public Enrollment(int enrollmentId, List<String> studentNumbers, String courseName) {
        this.enrollmentId = enrollmentId;
        this.studentNumbers = studentNumbers;
        this.courseName = courseName;
    }

    // DAO için constructor
    public Enrollment(int[] studentIds, String courseName) {
        this.studentIds = studentIds;
        this.courseName = courseName;
    }

    // Getter / Setter
    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int id) { this.enrollmentId = id; }

    public int[] getStudentIds() { return studentIds; }
    public void setStudentIds(int[] ids) { this.studentIds = ids; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String name) { this.courseName = name; }

    public List<String> getStudentNumbers() { return studentNumbers; }
}
