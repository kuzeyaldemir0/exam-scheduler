package examschd.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
            String courseId = enrollment.getCourseName(); // ImportService'ten map ile ID gelmiş olmalı

            for (int studentId : studentIds) {
                ps.setInt(1, studentId);
                ps.setString(2, courseId); // artık course_id
                ps.executeUpdate();
            }
        }
    }

    @Override
    public List<Enrollment> getAll() {
        List<Enrollment> list = new ArrayList<>();

        String sql = "SELECT student_id, course_id FROM Enrollments";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Enrollment e = new Enrollment(new int[]{ rs.getInt("student_id") }, rs.getString("course_id"));
                list.add(e);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    @Override
    public void clear() throws SQLException {
        try (Connection conn = DB.getConnection();
            PreparedStatement ps =
                    conn.prepareStatement("DELETE FROM Enrollments")) {
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteByIds(List<Integer> ids) throws SQLException {
        String sql = "DELETE FROM Enrollments WHERE student_id=?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int id : ids) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
    }
}
