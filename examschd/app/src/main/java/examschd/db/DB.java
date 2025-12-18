package examschd.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DB {

    private static final String DEFAULT_URL = "jdbc:sqlite:examscheduler.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            String url = System.getProperty("examschd.db.url", DEFAULT_URL);
            return DriverManager.getConnection(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
