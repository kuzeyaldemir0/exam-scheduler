package examschd.dao;

import examschd.model.Classroom;
import java.util.List;
import java.sql.SQLException;


public interface ClassroomDAO {
    void insert(Classroom classroom) throws SQLException;
    List<Classroom> getAll() throws SQLException;
}
