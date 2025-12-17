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

        String sql = "SELECT rowid, classroom_name, capacity FROM Classroom";

        try (Connection conn = DB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Classroom c = new Classroom(
                    rs.getInt("rowid"),           
                    rs.getString("classroom_name"),
                    rs.getInt("capacity")
                );
                list.add(c);
            }
        }

        return list;
    }

    //clears all table data
    @Override
    public void clear() throws SQLException {
    try (Connection conn = DB.getConnection();
         PreparedStatement ps =
             conn.prepareStatement("DELETE FROM Classroom")) {
        ps.executeUpdate();
    }
}
    //deletes classrooms by their IDs
    @Override
    public void deleteByIds(List<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return;

        StringBuilder sql = new StringBuilder(
                "DELETE FROM Classroom WHERE rowid IN (");

        for (int i = 0; i < ids.size(); i++) {
            sql.append("?");
            if (i < ids.size() - 1) sql.append(",");
        }
        sql.append(")");

        try (Connection conn = DB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            ps.executeUpdate();
        }
    }

}
