package examschd.service;

import examschd.daoimpl.BaseDaoTest;
import examschd.model.Classroom;
import examschd.model.Course;
import examschd.model.Enrollment;
import examschd.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImportServiceTest extends BaseDaoTest {

    private ImportService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws SQLException {
        service = new ImportService();
        service.loadExistingData();
    }


    private String writeFile(String filename, String content) throws Exception {
        Path p = tempDir.resolve(filename);
        Files.writeString(p, content);
        return p.toString();
    }

    private String createStudentsCSV(String... students) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("ALL OF THE STUDENTS IN THE SYSTEM\n");
        for (String s : students) sb.append(s).append("\n");
        return writeFile("students.csv", sb.toString());
    }

    private String createCoursesCSV(String... courses) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("ALL OF THE COURSES IN THE SYSTEM\n");
        for (String c : courses) sb.append(c).append("\n");
        return writeFile("courses.csv", sb.toString());
    }

    private String createClassroomsCSV(String... classrooms) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("ALL OF THE CLASSROOMS; AND THEIR CAPACITIES IN THE SYSTEM\n");
        for (String cr : classrooms) sb.append(cr).append("\n");
        return writeFile("classrooms.csv", sb.toString());
    }

    private String createEnrollmentsCSV(String... lines) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) sb.append(line).append("\n");
        return writeFile("enrollments.csv", sb.toString());
    }

    @Test
    void hasStudents_whenEmpty_returnsFalse() throws SQLException {
        assertFalse(service.hasStudents());
    }

    @Test
    void hasStudents_afterImport_returnsTrue() throws Exception {
        String filePath = createStudentsCSV("Std_ID_001", "Std_ID_002");
        service.importStudents(filePath);
        assertTrue(service.hasStudents());
    }

    @Test
    void importStudents_shouldInsertStudents() throws Exception {
        String filePath = createStudentsCSV("Std_ID_001", "Std_ID_002", "Std_ID_003");

        service.importStudents(filePath);

        List<Student> students = service.getAllStudents();
        assertEquals(3, students.size());
        assertTrue(students.stream().anyMatch(s -> "Std_ID_001".equals(s.getStudentName())));
        assertTrue(students.stream().anyMatch(s -> "Std_ID_002".equals(s.getStudentName())));
        assertTrue(students.stream().anyMatch(s -> "Std_ID_003".equals(s.getStudentName())));
    }

    @Test
    void importStudents_duplicateStudentNumber_shouldBeIgnored() throws Exception {
        String filePath = createStudentsCSV("Std_ID_001", "Std_ID_001");

        service.importStudents(filePath);

        List<Student> students = service.getAllStudents();
        assertEquals(1, students.size());
        assertEquals("Std_ID_001", students.get(0).getStudentName());
    }

    @Test
    void importStudents_multipleImports_shouldNotDuplicateExisting() throws Exception {
        String filePath1 = createStudentsCSV("Std_ID_001", "Std_ID_002");
        service.importStudents(filePath1);

        ImportService newService = new ImportService();
        newService.loadExistingData();

        String filePath2 = createStudentsCSV("Std_ID_002", "Std_ID_003");
        newService.importStudents(filePath2);

        assertEquals(3, newService.getAllStudents().size());
    }

    @Test
    void importStudents_emptyFile_shouldNotThrow() throws Exception {
        String filePath = createStudentsCSV();

        assertDoesNotThrow(() -> service.importStudents(filePath));
        assertTrue(service.getAllStudents().isEmpty());
    }


    @Test
    void importCourses_shouldInsertCourses() throws Exception {
        String filePath = createCoursesCSV("CourseCode_01", "CourseCode_02", "CourseCode_03");

        service.importCourses(filePath);

        List<Course> courses = service.getAllCourses();
        assertEquals(3, courses.size());
        assertTrue(courses.stream().anyMatch(c -> "CourseCode_01".equals(c.getCourseName())));
        assertTrue(courses.stream().anyMatch(c -> "CourseCode_02".equals(c.getCourseName())));
        assertTrue(courses.stream().anyMatch(c -> "CourseCode_03".equals(c.getCourseName())));
    }

    @Test
    void importCourses_duplicateCourseName_shouldBeIgnored() throws Exception {
        String filePath = createCoursesCSV("CourseCode_01", "CourseCode_01");

        service.importCourses(filePath);

        List<Course> courses = service.getAllCourses();
        assertEquals(1, courses.size());
        assertEquals("CourseCode_01", courses.get(0).getCourseName());
    }

    @Test
    void importCourses_emptyFile_shouldNotThrow() throws Exception {
        String filePath = createCoursesCSV();

        assertDoesNotThrow(() -> service.importCourses(filePath));
        assertTrue(service.getAllCourses().isEmpty());
    }


    @Test
    void importClassrooms_shouldInsertClassrooms() throws Exception {
        String filePath = createClassroomsCSV(
                "Classroom_01;40",
                "Classroom_02;40",
                "Classroom_03;40"
        );

        service.importClassrooms(filePath);

        List<Classroom> classrooms = service.getAllClassrooms();
        assertEquals(3, classrooms.size());
        assertTrue(classrooms.stream().anyMatch(c -> "Classroom_01".equals(c.getName()) && c.getCapacity() == 40));
        assertTrue(classrooms.stream().anyMatch(c -> "Classroom_02".equals(c.getName()) && c.getCapacity() == 40));
        assertTrue(classrooms.stream().anyMatch(c -> "Classroom_03".equals(c.getName()) && c.getCapacity() == 40));
    }

    @Test
    void importClassrooms_duplicateName_shouldBeIgnored() throws Exception {
        String filePath = createClassroomsCSV("Classroom_01;40", "Classroom_01;60");

        service.importClassrooms(filePath);

        List<Classroom> classrooms = service.getAllClassrooms();
        assertEquals(1, classrooms.size());
        assertEquals("Classroom_01", classrooms.get(0).getName());
        assertEquals(40, classrooms.get(0).getCapacity());
    }

    @Test
    void importClassrooms_emptyFile_shouldNotThrow() throws Exception {
        String filePath = createClassroomsCSV();

        assertDoesNotThrow(() -> service.importClassrooms(filePath));
        assertTrue(service.getAllClassrooms().isEmpty());
    }

    @Test
    void importEnrollments_shouldInsertOneRowPerStudentCoursePair() throws Exception {
        String studentsFile = createStudentsCSV("Std_ID_001", "Std_ID_002", "Std_ID_003");
        String coursesFile = createCoursesCSV("CourseCode_01", "CourseCode_02");

        service.importStudents(studentsFile);
        service.importCourses(coursesFile);
        service.loadExistingData();

        String enrollmentsFile = createEnrollmentsCSV(
                "CourseCode_01",
                "['Std_ID_001', 'Std_ID_002']",
                "CourseCode_02",
                "['Std_ID_003']"
        );

        service.importEnrollments(enrollmentsFile);

        List<Enrollment> rows = service.getAllEnrollments();
        assertEquals(3, rows.size());

        assertEquals(2, rows.stream().filter(e -> "CourseCode_01".equals(e.getCourseName())).count());
        assertEquals(1, rows.stream().filter(e -> "CourseCode_02".equals(e.getCourseName())).count());

        assertTrue(rows.stream().allMatch(e -> e.getStudentIds() != null && e.getStudentIds().length == 1));
    }

    @Test
    void importEnrollments_unknownCourse_shouldSkip() throws Exception {
        String studentsFile = createStudentsCSV("Std_ID_001");
        service.importStudents(studentsFile);
        service.loadExistingData();

        String enrollmentsFile = createEnrollmentsCSV(
                "CourseCode_999",
                "['Std_ID_001']"
        );

        service.importEnrollments(enrollmentsFile);

        assertTrue(service.getAllEnrollments().isEmpty());
    }


    @Test
    void fullImportWorkflow_shouldWorkCorrectly() throws Exception {
        String studentsFile = createStudentsCSV("Std_ID_001", "Std_ID_002", "Std_ID_003");
        String coursesFile = createCoursesCSV("CourseCode_01", "CourseCode_02");
        String classroomsFile = createClassroomsCSV("Classroom_01;40", "Classroom_02;40");

        service.importStudents(studentsFile);
        service.importCourses(coursesFile);
        service.importClassrooms(classroomsFile);
        service.loadExistingData();

        String enrollmentsFile = createEnrollmentsCSV(
                "CourseCode_01",
                "['Std_ID_001', 'Std_ID_002']",
                "CourseCode_02",
                "['Std_ID_003']"
        );
        service.importEnrollments(enrollmentsFile);

        assertEquals(3, service.getAllStudents().size());
        assertEquals(2, service.getAllCourses().size());
        assertEquals(2, service.getAllClassrooms().size());
        assertEquals(3, service.getAllEnrollments().size());
    }
}
