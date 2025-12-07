package examschd.dao;

import examschd.model.Course;
import java.sql.SQLException;


public interface CourseDAO {
    void insert(Course course) throws SQLException;
}
