package examschd.daoimpl;

import examschd.model.Course;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseDAOImplTest extends BaseDaoTest {
    private CourseDAOImpl dao;

    @BeforeEach
    void setUp() throws SQLException {
        dao = new CourseDAOImpl();
    }

    private Course findById(List<Course> all, int id) {
        return all.stream()
                .filter(c -> c.getCourseId() == id)
                .findFirst()
                .orElse(null);
    }

    @Test
    void getAll_whenEmpty_returnsEmptyList() {
        List<Course> all = dao.getAll();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    void insert_thenGetAll_mapsFieldsCorrectly() throws SQLException {
        dao.insert(new Course(1, "CourseCode_01"));
        dao.insert(new Course(2, "CourseCode_02"));

        List<Course> all = dao.getAll();
        assertEquals(2, all.size());

        Course c1 = findById(all, 1);
        Course c2 = findById(all, 2);

        assertNotNull(c1);
        assertEquals("CourseCode_01", c1.getCourseName());

        assertNotNull(c2);
        assertEquals("CourseCode_02", c2.getCourseName());
    }

    @Test
    void insert_duplicateCourseId_shouldNotCreateSecondRow() throws SQLException {
        dao.insert(new Course(10, "First"));
        dao.insert(new Course(10, "Second"));

        List<Course> all = dao.getAll();
        assertEquals(1, all.size());

        Course c = all.get(0);
        assertEquals(10, c.getCourseId());
        assertEquals("First", c.getCourseName());
    }

    @Test
    void deleteByCourseIds_shouldDeleteOnlySelectedCourses() throws SQLException {
        dao.insert(new Course(1, "A"));
        dao.insert(new Course(2, "B"));
        dao.insert(new Course(3, "C"));

        dao.deleteByCourseIds(List.of(2));

        List<Course> all = dao.getAll();
        assertNotNull(findById(all, 1));
        assertNull(findById(all, 2));
        assertNotNull(findById(all, 3));
    }

    @Test
    void deleteByCourseIds_withEmptyList_shouldDoNothing() throws SQLException {
        dao.insert(new Course(1, "A"));

        dao.deleteByCourseIds(List.of());

        List<Course> all = dao.getAll();
        assertNotNull(findById(all, 1));
        assertEquals(1, all.size());
    }

    @Test
    void clear_shouldRemoveAllRows() throws SQLException {
        dao.insert(new Course(1, "A"));
        dao.insert(new Course(2, "B"));

        dao.clear();

        List<Course> all = dao.getAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void update_existingCourse_shouldReturnTrue() throws SQLException {
        dao.insert(new Course(1, "OldName"));

        Course course = new Course(1, "NewName");
        boolean updated = dao.update(course);

        assertTrue(updated);

        List<Course> all = dao.getAll();
        Course c = findById(all, 1);
        assertNotNull(c);
        assertEquals("NewName", c.getCourseName());
    }

    @Test
    void update_nonExistingCourse_shouldReturnFalse() throws SQLException {
        Course fake = new Course(9999, "FakeCourse");
        boolean updated = dao.update(fake);

        assertFalse(updated);
    }
}
