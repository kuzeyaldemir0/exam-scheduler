package examschd.dao;

import examschd.model.Student;
import java.sql.SQLException;


public interface StudentDAO {
    void insert(Student student) throws SQLException;
}
