package examschd.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import examschd.dao.EnrollmentDAO;
import examschd.db.DB;
import examschd.model.Enrollment;

public class EnrollmentDAOImpl implements EnrollmentDAO {

    private static final String INSERT_SQL =
            "INSERT INTO Enrollments (student_id, course_id) VALUES (?, ?)";

    @Override
    public void insert(Enrollment enrollment) throws SQLException {
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            int[] studentIds = enrollment.getStudentIds();
            String courseName = enrollment.getCourseName();

            for (int studentId : studentIds) {
                ps.setInt(1, studentId);   // Öğrenci ID
                ps.setString(2, courseName);    // Ders ID
                ps.executeUpdate();        // Her öğrenci için insert
            }
        }
    }
}
