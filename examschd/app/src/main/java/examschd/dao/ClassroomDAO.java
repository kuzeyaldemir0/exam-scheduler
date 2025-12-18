package examschd.dao;

import examschd.model.Classroom;
import java.util.List;
import java.sql.SQLException;


public interface ClassroomDAO {
    void insert(Classroom classroom) throws SQLException;
    List<Classroom> getAll() throws SQLException;
    void clear() throws SQLException; 
    void deleteByIds(List<Integer> ids) throws SQLException;
    boolean update(Classroom classroom) throws SQLException;
}
