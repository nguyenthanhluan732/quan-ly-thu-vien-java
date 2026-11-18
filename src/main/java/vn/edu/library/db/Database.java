package vn.edu.library.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private Database() {}

    public static Connection getConnection() throws SQLException {
        String url = setting("LIBRARY_DB_URL",
                "jdbc:sqlserver://localhost:1433;databaseName=QuanLyThuVien;encrypt=true;trustServerCertificate=true");
        String user = setting("LIBRARY_DB_USER", "sa");
        String password = setting("LIBRARY_DB_PASSWORD", "");
        if (password.isBlank()) {
            throw new SQLException("Chua cau hinh mat khau SQL Server. Hay dat bien moi truong LIBRARY_DB_PASSWORD.");
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static String setting(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}
