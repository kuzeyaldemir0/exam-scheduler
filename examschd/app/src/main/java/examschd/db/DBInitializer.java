package examschd.db;

import java.sql.*;

public class DBInitializer {

    public static void initialize() {
        try (Connection conn = DB.getConnection();
             Statement st = conn.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS Students (
                    student_id TEXT PRIMARY KEY
                )
            """);

            if (!columnExists(conn, "Students", "student_name")) {
                st.execute("ALTER TABLE Students ADD COLUMN student_name TEXT");
            }

            st.execute("""
                CREATE TABLE IF NOT EXISTS Courses (
                    course_id TEXT PRIMARY KEY,
                    course_name TEXT,
                    duration INTEGER
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Enrollments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id TEXT,
                    course_id TEXT,
                    FOREIGN KEY(student_id) REFERENCES Students(student_id),
                    FOREIGN KEY(course_id) REFERENCES Courses(course_id)
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Exams (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    course_id TEXT,
                    duration INTEGER,
                    FOREIGN KEY(course_id) REFERENCES Courses(course_id)
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Classroom (
                    classroom_name TEXT PRIMARY KEY,
                    capacity INTEGER
                )
            """);

            System.out.println("Database schema created / migrated.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean columnExists(Connection conn, String table, String column)
            throws SQLException {

        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, table, column)) {
            return rs.next();
        }
    }
}
