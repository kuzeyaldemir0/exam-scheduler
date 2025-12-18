package examschd.daoimpl;

import examschd.model.Student;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentDAOImplTest extends BaseDaoTest {
    private StudentDAOImpl dao;

    @BeforeEach
    void setUp() throws SQLException {
        dao = new StudentDAOImpl();
    }

    private Student findById(List<Student> all, int id) {
        return all.stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Test
    void getAll_whenEmpty_returnsEmptyList() {
        List<Student> all = dao.getAll();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    void insert_thenGetAll_mapsFieldsCorrectly() throws SQLException {
        dao.insert(new Student(1, "Std_ID_001"));
        dao.insert(new Student(2, "Std_ID_002"));

        List<Student> all = dao.getAll();
        assertEquals(2, all.size());

        Student s1 = findById(all, 1);
        Student s2 = findById(all, 2);

        assertNotNull(s1);
        assertEquals("Std_ID_001", s1.getStudentName());

        assertNotNull(s2);
        assertEquals("Std_ID_002", s2.getStudentName());
    }

    @Test
    void insert_duplicateId_shouldBeIgnored_andNotCreateSecondRow() throws SQLException {
        dao.insert(new Student(10, "FirstName"));
        dao.insert(new Student(10, "SecondName"));

        List<Student> all = dao.getAll();
        assertEquals(1, all.size());

        Student s = all.get(0);
        assertEquals(10, s.getId());
        assertEquals("FirstName", s.getStudentName());
    }

    @Test
    void deleteByIds_shouldDeleteOnlySelectedStudents() throws SQLException {
        dao.insert(new Student(1, "A"));
        dao.insert(new Student(2, "B"));
        dao.insert(new Student(3, "C"));

        dao.deleteByIds(List.of(2));

        List<Student> all = dao.getAll();
        assertNotNull(findById(all, 1));
        assertNull(findById(all, 2));
        assertNotNull(findById(all, 3));
    }

    @Test
    void deleteByIds_withEmptyList_shouldDoNothing() throws SQLException {
        dao.insert(new Student(1, "A"));

        dao.deleteByIds(List.of());

        List<Student> all = dao.getAll();
        assertEquals(1, all.size());
        assertNotNull(findById(all, 1));
    }

    @Test
    void clear_shouldRemoveAllRows() throws SQLException {
        dao.insert(new Student(1, "A"));
        dao.insert(new Student(2, "B"));

        dao.clear();

        List<Student> all = dao.getAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void update_existingStudent_shouldReturnTrue() throws SQLException {
        dao.insert(new Student(1, "OldName"));

        Student student = new Student(1, "NewName");
        boolean updated = dao.update(student);

        assertTrue(updated);

        List<Student> all = dao.getAll();
        Student s = findById(all, 1);
        assertNotNull(s);
        assertEquals("NewName", s.getStudentName());
    }

    @Test
    void update_nonExistingStudent_shouldReturnFalse() throws SQLException {
        Student fake = new Student(9999, "FakeStudent");
        boolean updated = dao.update(fake);

        assertFalse(updated);
    }
}
