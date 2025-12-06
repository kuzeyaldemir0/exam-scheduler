package examschd.service;

import examschd.dao.*;
import examschd.daoimpl.*;
import examschd.model.*;
import examschd.service.readers.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportService {

    private final StudentDAO studentDAO = new StudentDAOImpl();
    private final CourseDAO courseDAO = new CourseDAOImpl();
    private final ClassroomDAO classroomDAO = new ClassroomDAOImpl();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAOImpl();

    private final StudentCsvReader studentReader = new StudentCsvReader();
    private final CourseCsvReader courseReader = new CourseCsvReader();
    private final ClassroomCsvReader classroomReader = new ClassroomCsvReader();
    private final EnrollmentCsvReader enrollmentReader = new EnrollmentCsvReader();

    // ID sayacı
    private int studentIdCounter = 1;
    private int courseIdCounter = 1;
    private int classroomIdCounter = 1;
    private int enrollmentIdCounter = 1;

    // MAP'ler - Enrollment için gerekli
    private Map<String, Integer> studentIdMap = new HashMap<>(); // key = studentNumber, value = studentId
    private Map<String, Integer> courseIdMap = new HashMap<>();  // key = courseName, value = courseId

    public void importStudents(String filePath) throws Exception {
        List<Student> students = studentReader.read(filePath);

        for (Student s : students) {
            s.setId(studentIdCounter++);
            studentDAO.insert(s);

            // Map'te sakla: studentNumber -> studentId
            studentIdMap.put(s.getName(), s.getId());
        }

        System.out.println("Students imported: " + students.size());
    }

    public void importCourses(String filePath) throws Exception {
        List<Course> courses = courseReader.read(filePath);

        for (Course c : courses) {
            c.setCourseId(courseIdCounter++);
            courseDAO.insert(c);

            // Map'te sakla: courseName -> courseId
            courseIdMap.put(c.getCourseName(), c.getCourseId());
        }

        System.out.println("Courses imported: " + courses.size());
    }

    public void importClassrooms(String filePath) throws Exception {
        List<Classroom> classrooms = classroomReader.read(filePath);

        for (Classroom cr : classrooms) {
            cr.setClassroomId(classroomIdCounter++);
            classroomDAO.insert(cr);
        }

        System.out.println("Classrooms imported: " + classrooms.size());
    }

    public void importEnrollments(String filePath) throws Exception {
    List<Enrollment> enrollments = enrollmentReader.read(filePath);

    for (Enrollment e : enrollments) {
        e.setEnrollmentId(enrollmentIdCounter++);

        // CSV'den gelen öğrenciler (String listesi)
        List<String> studentNumbers = e.getStudentNumbers();

        // Öğrenci ID'lerini array olarak saklamak istersen:
        int[] studentIds = studentNumbers.stream()
            .mapToInt(sn -> {
                Integer id = studentIdMap.get(sn);
                if (id == null) {
                    System.err.println("Unknown student: " + sn);
                    return 0;
                }
                return id;
            }).toArray();

        e.setStudentIds(studentIds); // array set
        // courseName direkt CSV'den geliyor, map'e gerek yok
        String courseName = e.getCourseName(); 
        e.setCourseName(courseName);

        enrollmentDAO.insert(e); // DAO da courseName kullanacak
    }

    System.out.println("Enrollments imported: " + enrollments.size());
}

}
