package examschd.daoimpl;

import examschd.dao.CourseDAO;
import examschd.model.Course;
import examschd.db.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDAOImpl implements CourseDAO {

    @Override
    public void insert(Course course) throws SQLException {
        String sql = "INSERT INTO Courses ( course_id, course_name) VALUES (?, ?)";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, course.getCourseId());
            ps.setString(2, course.getCourseName());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Course> getAll() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT course_id, course_name FROM Courses";

        try (Connection conn = DB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Course c = new Course(rs.getInt("course_id"), rs.getString("course_name"));
                courses.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }

}
