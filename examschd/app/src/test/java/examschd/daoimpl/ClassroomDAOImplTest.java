package examschd.daoimpl;

import examschd.model.Classroom;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassroomDAOImplTest extends BaseDaoTest {
    private ClassroomDAOImpl dao;

    @BeforeEach
    void setUp() throws SQLException {
        dao = new ClassroomDAOImpl();
    }

    @Test
    void getAll_whenEmpty_returnsEmptyList() throws SQLException {
        List<Classroom> all = dao.getAll();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    void insert_thenGetAll_mapsFields_andRowidIsPositive() throws SQLException {
        dao.insert(new Classroom(0, "Classroom_01", 40));

        List<Classroom> all = dao.getAll();
        assertEquals(1, all.size());

        Classroom c = all.get(0);
        assertEquals("Classroom_01", c.getName());
        assertEquals(40, c.getCapacity());
        assertTrue(c.getClassroomId() > 0, "Row id should be positive");
    }

    @Test
    void insert_duplicatePrimaryKey_shouldThrowSQLException() throws SQLException {
        dao.insert(new Classroom(0, "DUP", 10));

        assertThrows(SQLException.class, () -> {
            dao.insert(new Classroom(0, "DUP", 20));
        }, "Duplicate classroom name should throw SQLException");
    }

    @Test
    void deleteByIds_shouldDeleteSelectedRow() throws SQLException {
        dao.insert(new Classroom(0, "A", 10));
        dao.insert(new Classroom(0, "B", 20));

        List<Classroom> all = dao.getAll();
        assertEquals(2, all.size());

        Classroom b = all.stream()
                .filter(c -> "B".equals(c.getName()))
                .findFirst()
                .orElseThrow();

        dao.deleteByIds(List.of(b.getClassroomId()));

        List<Classroom> after = dao.getAll();
        assertEquals(1, after.size());
        assertEquals("A", after.get(0).getName());
    }

    @Test
    void clear_shouldRemoveAllRows() throws SQLException {
        dao.insert(new Classroom(0, "X", 10));
        dao.insert(new Classroom(0, "Y", 20));

        dao.clear();

        List<Classroom> after = dao.getAll();
        assertTrue(after.isEmpty());
    }

    @Test
    void update_existingClassroom_shouldReturnTrue() throws SQLException {
        dao.insert(new Classroom(0, "OLD", 10));

        List<Classroom> all = dao.getAll();
        Classroom classroom = all.get(0);

        classroom.setName("NEW");
        classroom.setCapacity(50);

        boolean updated = dao.update(classroom);
        assertTrue(updated);

        List<Classroom> after = dao.getAll();
        assertEquals(1, after.size());
        assertEquals("NEW", after.get(0).getName());
        assertEquals(50, after.get(0).getCapacity());
    }

    @Test
    void update_nonExistingClassroom_shouldReturnFalse() throws SQLException {
        Classroom fake = new Classroom(9999, "FAKE", 10);
        boolean updated = dao.update(fake);
        assertFalse(updated);
    }
}
