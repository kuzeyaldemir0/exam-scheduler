package examschd.dao;

import examschd.model.Enrollment;
import java.util.List;
import java.sql.SQLException;


public interface EnrollmentDAO {
    void insert(Enrollment enrollment) throws SQLException;
    List<Enrollment> getAll() throws SQLException;
}
