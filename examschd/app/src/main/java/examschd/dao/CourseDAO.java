package examschd.dao;

import examschd.model.Course;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
import java.util.ArrayList;


public interface CourseDAO {
    void insert(Course course) throws SQLException;
}
