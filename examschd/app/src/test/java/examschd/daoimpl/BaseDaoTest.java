package examschd.daoimpl;

import examschd.db.DB;
import examschd.db.DBInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class BaseDaoTest {
    protected static final String TEST_DB_FILE = "build/examscheduler-test.db";

    @BeforeAll
    static void setupTestDb() throws Exception {
        System.setProperty("examschd.db.url", "jdbc:sqlite:" + TEST_DB_FILE);

        Path dbPath = Path.of(TEST_DB_FILE);
        Files.createDirectories(dbPath.getParent());
        Files.deleteIfExists(dbPath);

        DBInitializer.initialize();
        System.out.println("✓ Test DB created: " + TEST_DB_FILE);
    }

    @BeforeEach
    void resetDatabase() throws Exception {
        try (Connection conn = DB.getConnection()) {
            assertNotNull(conn, "DB.getConnection() returned null");

            String url = conn.getMetaData().getURL();
            if (url == null || !url.contains("examscheduler-test.db")) {
                throw new IllegalStateException("Not using the test database! JDBC URL = " + url);
            }
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM Enrollments");
                st.executeUpdate("DELETE FROM Exams");
                st.executeUpdate("DELETE FROM Courses");
                st.executeUpdate("DELETE FROM Students");
                st.executeUpdate("DELETE FROM Classroom");
            }
        }
    }
}
