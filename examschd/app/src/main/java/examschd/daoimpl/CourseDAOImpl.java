package examschd.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import examschd.dao.CourseDAO;
import examschd.model.Course;
import examschd.db.DB;
import java.sql.SQLException;

public class CourseDAOImpl implements CourseDAO {

    @Override
    public void insert(Course course) throws SQLException {
        String sql = "INSERT INTO courses(course_id, course_code) VALUES(?, ?)";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, course.getCourseId());
            ps.setString(2, course.getCourseName());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
