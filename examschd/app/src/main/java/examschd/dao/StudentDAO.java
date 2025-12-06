package examschd.dao;

import examschd.model.Student;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
import java.util.ArrayList;


public interface StudentDAO {
    void insert(Student student) throws SQLException;
}
