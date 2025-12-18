package examschd.daoimpl;

import examschd.model.Student;
import examschd.dao.StudentDAO;
import examschd.db.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDAOImpl implements StudentDAO {

    @Override
    public void insert(Student student) throws SQLException {
        String sql = "INSERT OR IGNORE INTO Students (student_id, student_name) VALUES (?, ?)";

        try (Connection conn = DB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, student.getId());
            ps.setString(2, student.getStudentName());
            ps.executeUpdate();
        }
    }


    @Override
    public List<Student> getAll() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT student_id, student_name FROM Students";

        try (Connection conn = DB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Student s = new Student(rs.getInt("student_id"), rs.getString("student_name"));
                list.add(s);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    @Override
    public void clear() throws SQLException {
        // First delete dependent records (Enrollments)
        String sqlEnrollments = "DELETE FROM Enrollments";
        String sqlStudents = "DELETE FROM Students";

        try (Connection conn = DB.getConnection();
             PreparedStatement psEnr = conn.prepareStatement(sqlEnrollments);
             PreparedStatement psStd = conn.prepareStatement(sqlStudents)) {
             
            psEnr.executeUpdate();
            psStd.executeUpdate();
        }
    }

    @Override
    public void deleteByIds(List<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // Delete associated enrollments (Manual CASCADE)
        StringBuilder sqlEnr = new StringBuilder("DELETE FROM Enrollments WHERE student_id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sqlEnr.append("?");
            if (i < ids.size() - 1) sqlEnr.append(", ");
        }
        sqlEnr.append(")");

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlEnr.toString())) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            ps.executeUpdate();
        }

        // Delete students

        StringBuilder sql = new StringBuilder("DELETE FROM Students WHERE student_id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sql.append("?");
            if (i < ids.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }

            ps.executeUpdate();
        }
    }

    @Override
    public boolean update(Student student) throws SQLException {
        String sql = "UPDATE Students SET student_name = ? WHERE student_id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getStudentName());
            ps.setInt(2, student.getId());
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

}
