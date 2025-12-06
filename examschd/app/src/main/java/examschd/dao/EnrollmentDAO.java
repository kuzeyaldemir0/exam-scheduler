package examschd.dao;

import examschd.model.Enrollment;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
import java.util.ArrayList;


public interface EnrollmentDAO {
    void insert(Enrollment enrollment) throws SQLException;
}
