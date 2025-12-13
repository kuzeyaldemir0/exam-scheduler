package examschd.db;

import java.sql.Connection;
import java.sql.Statement;

public class DBInitializer {

    public static void initialize() {
        try (Connection conn = DB.getConnection();
             Statement st = conn.createStatement()) {

            // Students table
            st.execute("CREATE TABLE IF NOT EXISTS Students (" +
                    "student_id TEXT PRIMARY KEY," +
                    "student_name TEXT)");

            // Courses table
            st.execute("CREATE TABLE IF NOT EXISTS Courses (" +
                    "course_id TEXT PRIMARY KEY," +
                    "course_name TEXT," +
                    "duration INTEGER)");

            // Enrollments table
            st.execute("CREATE TABLE IF NOT EXISTS Enrollments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "student_id TEXT," +
                    "course_id TEXT," +
                    "FOREIGN KEY(student_id) REFERENCES Students(student_id)," +
                    "FOREIGN KEY(course_id) REFERENCES Courses(course_id))");

            // Exams table
            st.execute("CREATE TABLE IF NOT EXISTS Exams (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "course_id TEXT," +
                    "duration INTEGER," +
                    "FOREIGN KEY(course_id) REFERENCES Courses(course_id))");

            // Classrooms table
            st.execute("CREATE TABLE IF NOT EXISTS Classroom ("+
                    "classroom_name TEXT PRIMARY KEY," +
                    "capacity INTEGER)");

            System.out.println("Database schema created.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}