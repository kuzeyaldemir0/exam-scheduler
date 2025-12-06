package examschd.daoimpl;

import java.util.*;

import examschd.model.Student;
import examschd.dao.StudentDAO;
import examschd.db.DB;
import java.sql.SQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class StudentDAOImpl  implements StudentDAO {

    private static final String INSERT_SQL =
            "INSERT INTO Students (student_id) VALUES (?)";

    public void insert(Student student) throws SQLException {
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setInt(1, student.getId());
            ps.executeUpdate();
        }
    }
}
