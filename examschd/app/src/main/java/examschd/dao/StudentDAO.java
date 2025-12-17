package examschd.dao;

import examschd.model.Student;
import java.util.List;
import java.sql.SQLException;


public interface StudentDAO {
    void insert(Student student) throws SQLException;
    List<Student> getAll() throws SQLException;
    void clear() throws SQLException;
    void deleteByIds(List<Integer> ids) throws SQLException;
}
