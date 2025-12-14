package examschd.service;

import examschd.dao.*;
import examschd.daoimpl.*;
import examschd.model.*;
import examschd.service.readers.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.SQLException;

public class ImportService {

    private final StudentDAO studentDAO = new StudentDAOImpl();
    private final CourseDAO courseDAO = new CourseDAOImpl();
    private final ClassroomDAO classroomDAO = new ClassroomDAOImpl();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAOImpl();

    // ID sayacı
    private int studentIdCounter = 1;
    private int courseIdCounter = 1;
    private int classroomIdCounter = 1;
    private int enrollmentIdCounter = 1;

    // Map'ler
    private final Map<String, Integer> studentIdMap = new HashMap<>(); // studentNumber -> ID
    private final Map<String, Integer> courseIdMap = new HashMap<>();  // courseName -> ID
    private final Map<String, Integer> classroomIdMap = new HashMap<>(); // classroomName -> ID

    // Başlangıçta DB’den mevcut verileri yükle
    public void loadExistingData() throws SQLException {
        for (Student s : studentDAO.getAll()) {
            studentIdMap.put(s.getStudentName(), s.getId());
            studentIdCounter = Math.max(studentIdCounter, s.getId() + 1);
        }

        for (Course c : courseDAO.getAll()) {
            courseIdMap.put(c.getCourseName(), c.getCourseId());
            courseIdCounter = Math.max(courseIdCounter, c.getCourseId() + 1);
        }

        for (Classroom cr : classroomDAO.getAll()) {
            classroomIdMap.put(cr.getName(), cr.getClassroomId());
            classroomIdCounter = Math.max(classroomIdCounter, cr.getClassroomId() + 1);
        }

        for (Enrollment e : enrollmentDAO.getAll()) {
            enrollmentIdCounter = Math.max(enrollmentIdCounter, e.getEnrollmentId() + 1);
        }
    }

    public boolean hasStudents() throws SQLException {
        return !studentDAO.getAll().isEmpty();
    }

    public void importStudents(String filePath) throws Exception {
        List<Student> students = StudentCsvReader.read(filePath);

        for (Student s : students) {
            String key = s.getStudentName().trim();
            if (studentIdMap.containsKey(key)) continue; // zaten varsa atla

            s.setId(studentIdCounter++);
            studentDAO.insert(s);
            studentIdMap.put(key, s.getId());
        }

        System.out.println("Students imported: " + students.size());
    }

    public void importCourses(String filePath) throws Exception {
        List<Course> courses = CourseCsvReader.read(filePath);

        for (Course c : courses) {
            String key = c.getCourseName().trim();
            if (courseIdMap.containsKey(key)) continue; // zaten varsa atla

            c.setCourseId(courseIdCounter++);
            courseDAO.insert(c);
            courseIdMap.put(key, c.getCourseId());
        }

        System.out.println("Courses imported: " + courses.size());
    }

    public void importClassrooms(String filePath) throws Exception {
        List<Classroom> classrooms = ClassroomCsvReader.read(filePath);

        for (Classroom cr : classrooms) {
            String key = cr.getName().trim();
            if (classroomIdMap.containsKey(key)) continue; // zaten varsa atla

            cr.setClassroomId(classroomIdCounter++);
            classroomDAO.insert(cr);
            classroomIdMap.put(key, cr.getClassroomId());
        }

        System.out.println("Classrooms imported: " + classrooms.size());
    }

    public void importEnrollments(String filePath) throws Exception {
        List<Enrollment> enrollments = EnrollmentCsvReader.read(filePath);

        for (Enrollment e : enrollments) {
            e.setEnrollmentId(enrollmentIdCounter++);

            // Öğrenci ID’lerini al
            List<String> studentNumbers = e.getStudentNumbers();
            int[] studentIds = studentNumbers.stream()
                    .mapToInt(sn -> {
                        String cleanSn = sn.trim();
                        Integer id = studentIdMap.get(cleanSn);
                        if (id == null) {
                            System.err.println("Unknown student: " + cleanSn);
                            return 0;
                        }
                        return id;
                    }).toArray();
            e.setStudentIds(studentIds);

            // Kurs ID’yi al
            String courseName = e.getCourseName().trim().replace(";", "");
            Integer courseIdInt = courseIdMap.get(courseName);
            if (courseIdInt == null) {
                System.err.println("Unknown course: " + courseName);
                continue; // bilinmeyen kursu atla
            }

            // Keep the actual course name (already set from CSV), don't overwrite with ID

            enrollmentDAO.insert(e);
        }

        System.out.println("Enrollments imported: " + enrollments.size());
    }

    // DB’den veri çekme
    public List<Student> getAllStudents() throws SQLException {
        return studentDAO.getAll();
    }

    public List<Course> getAllCourses() throws SQLException {
        return courseDAO.getAll();
    }

    public List<Classroom> getAllClassrooms() throws SQLException {
        return classroomDAO.getAll();
    }

    public List<Enrollment> getAllEnrollments() throws SQLException {
        return enrollmentDAO.getAll();
    }
}
