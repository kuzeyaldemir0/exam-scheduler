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

    //clear all courses
    @Override
    public void clear() throws SQLException {
        try (Connection conn = DB.getConnection();
            PreparedStatement ps =
                conn.prepareStatement("DELETE FROM Courses")) {
            ps.executeUpdate();
        }
    }

    //delete courses by a list of course IDs
    @Override
    public void deleteByCourseIds(List<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return;

        StringBuilder sql = new StringBuilder(
            "DELETE FROM Courses WHERE course_id IN ("
        );

        for (int i = 0; i < ids.size(); i++) {
            sql.append("?");
            if (i < ids.size() - 1) sql.append(",");
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

}
