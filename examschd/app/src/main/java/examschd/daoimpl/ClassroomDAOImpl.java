package examschd.daoimpl;

import examschd.dao.ClassroomDAO;
import examschd.db.DB;
import examschd.model.Classroom;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassroomDAOImpl implements ClassroomDAO {

    @Override
    public void insert(Classroom classroom) throws SQLException {
        String sql = "INSERT INTO Classroom (classroom_name, capacity) VALUES (?, ?)";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, classroom.getName());
            ps.setInt(2, classroom.getCapacity());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Classroom> getAll() throws SQLException {
        List<Classroom> list = new ArrayList<>();

        String sql = "SELECT classroom_name, capacity FROM Classroom";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int id = 1;
            while (rs.next()) {
                Classroom c = new Classroom(
                        id,
                        rs.getString("classroom_name"),
                        rs.getInt("capacity")
                );
                id++;
                list.add(c);
            }
        }

        return list;
    }
}
