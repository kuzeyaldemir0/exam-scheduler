package examschd.dao;

import examschd.model.Classroom;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
import java.util.ArrayList;


public interface ClassroomDAO {
    void insert(Classroom classroom) throws SQLException;
    List<Classroom> getAll() throws SQLException;
}
