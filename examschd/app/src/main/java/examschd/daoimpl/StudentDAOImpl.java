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

}
