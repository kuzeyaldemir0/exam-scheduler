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
             PreparedStatement psEnr = conn.prepareStatement("DELETE FROM Enrollments");
             PreparedStatement psCourses = conn.prepareStatement("DELETE FROM Courses")) {
            
            psEnr.executeUpdate();
            psCourses.executeUpdate();
        }
    }

    //delete courses by a list of course IDs
    @Override
    public void deleteByCourseIds(List<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return;

        // 1. Delete associated enrollments (Manual CASCADE)
        StringBuilder sqlEnr = new StringBuilder("DELETE FROM Enrollments WHERE course_id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sqlEnr.append("?");
            if (i < ids.size() - 1) sqlEnr.append(",");
        }
        sqlEnr.append(")");

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlEnr.toString())) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            ps.executeUpdate();
        }

        // 2. Delete courses

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

    @Override
    public boolean update(Course course) throws SQLException {
        String sql = "UPDATE Courses SET course_name = ? WHERE course_id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, course.getCourseName());
            ps.setInt(2, course.getCourseId());
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

}
