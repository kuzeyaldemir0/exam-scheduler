package examschd.dao;

import examschd.model.Course;
import java.util.List;
import java.sql.SQLException;


public interface CourseDAO {
    void insert(Course course) throws SQLException;
    List<Course> getAll() throws SQLException;
    void clear() throws SQLException;
    void deleteByCourseIds(List<Integer> courseIds) throws SQLException;
}
