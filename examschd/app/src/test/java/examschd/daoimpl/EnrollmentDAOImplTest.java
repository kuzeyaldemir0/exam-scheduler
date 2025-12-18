package examschd.daoimpl;

import examschd.model.Course;
import examschd.model.Enrollment;
import examschd.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentDAOImplTest extends BaseDaoTest {
    private EnrollmentDAOImpl enrollmentDao;
    private StudentDAOImpl studentDao;
    private CourseDAOImpl courseDao;

    @BeforeEach
    void setUp() throws SQLException {
        enrollmentDao = new EnrollmentDAOImpl();
        courseDao = new CourseDAOImpl();
        studentDao = new StudentDAOImpl();

        studentDao.insert(new Student(1, "Std_ID_001"));
        studentDao.insert(new Student(2, "Std_ID_002"));
        studentDao.insert(new Student(3, "Std_ID_003"));

        courseDao.insert(new Course(1, "CourseCode_01"));
        courseDao.insert(new Course(2, "CourseCode_02"));
    }

    @Test
    void getAll_whenEmpty_returnsEmptyList() throws SQLException {
        enrollmentDao.clear();

        List<Enrollment> all = enrollmentDao.getAll();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    void insert_singleCourse_multipleStudents_createsMultipleRows() throws SQLException {
        Enrollment e = new Enrollment(new int[]{1, 2, 3}, "1");
        enrollmentDao.insert(e);

        List<Enrollment> all = enrollmentDao.getAll();
        assertEquals(3, all.size());

        assertTrue(all.stream().anyMatch(x ->
                x.getStudentIds()[0] == 1 && "1".equals(x.getCourseName())));
        assertTrue(all.stream().anyMatch(x ->
                x.getStudentIds()[0] == 2 && "1".equals(x.getCourseName())));
        assertTrue(all.stream().anyMatch(x ->
                x.getStudentIds()[0] == 3 && "1".equals(x.getCourseName())));
    }

    @Test
    void insert_twoCourses_createsRowsWithCorrectCourseId() throws SQLException {
        enrollmentDao.insert(new Enrollment(new int[]{1, 2}, "1"));
        enrollmentDao.insert(new Enrollment(new int[]{3}, "2"));

        List<Enrollment> all = enrollmentDao.getAll();
        assertEquals(3, all.size());

        assertTrue(all.stream().anyMatch(x ->
                x.getStudentIds()[0] == 1 && "1".equals(x.getCourseName())));
        assertTrue(all.stream().anyMatch(x ->
                x.getStudentIds()[0] == 2 && "1".equals(x.getCourseName())));
        assertTrue(all.stream().anyMatch(x ->
                x.getStudentIds()[0] == 3 && "2".equals(x.getCourseName())));
    }

    @Test
    void deleteByIds_shouldDeleteEnrollmentsByStudentId() throws SQLException {
        enrollmentDao.insert(new Enrollment(new int[]{1, 2, 3}, "1"));
        enrollmentDao.insert(new Enrollment(new int[]{2}, "2"));

        enrollmentDao.deleteByIds(List.of(2));

        List<Enrollment> all = enrollmentDao.getAll();

        assertTrue(all.stream().anyMatch(x -> x.getStudentIds()[0] == 1));
        assertTrue(all.stream().anyMatch(x -> x.getStudentIds()[0] == 3));

        assertFalse(all.stream().anyMatch(x -> x.getStudentIds()[0] == 2));
    }

    @Test
    void clear_shouldRemoveAllRows() throws SQLException {
        enrollmentDao.insert(new Enrollment(new int[]{1, 2}, "1"));
        assertFalse(enrollmentDao.getAll().isEmpty());

        enrollmentDao.clear();

        assertTrue(enrollmentDao.getAll().isEmpty());
    }

    @Test
    void enroll_newEnrollment_shouldReturnTrue() throws SQLException {
        boolean enrolled = enrollmentDao.enroll(1, 1);

        assertTrue(enrolled);

        List<Enrollment> all = enrollmentDao.getAll();
        assertEquals(1, all.size());
        assertEquals(1, all.get(0).getStudentIds()[0]);
    }

    @Test
    void withdraw_existingEnrollment_shouldReturnTrue() throws SQLException {
        enrollmentDao.enroll(1, 1);

        boolean withdrawn = enrollmentDao.withdraw(1, 1);

        assertTrue(withdrawn);
        assertTrue(enrollmentDao.getAll().isEmpty());
    }

    @Test
    void withdraw_nonExistingEnrollment_shouldReturnFalse() throws SQLException {
        boolean withdrawn = enrollmentDao.withdraw(1, 1);

        assertFalse(withdrawn);
    }

    @Test
    void updateCourseForStudent_existingEnrollment_shouldReturnTrue() throws SQLException {
        enrollmentDao.enroll(1, 1);

        boolean updated = enrollmentDao.updateCourseForStudent(1, 1, 2);

        assertTrue(updated);

        List<Enrollment> all = enrollmentDao.getAll();
        assertEquals(1, all.size());
        assertEquals("2", all.get(0).getCourseName());
    }

    @Test
    void updateCourseForStudent_nonExistingEnrollment_shouldReturnFalse() throws SQLException {
        boolean updated = enrollmentDao.updateCourseForStudent(1, 1, 2);

        assertFalse(updated);
    }
}
