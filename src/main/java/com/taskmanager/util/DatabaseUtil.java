package com.taskmanager.util;

import com.taskmanager.dao.UserDAO;
import com.taskmanager.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class DatabaseUtil {
    private static final String DRIVER;
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties config = loadConfig();
        DRIVER = config.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        URL = config.getProperty("db.url");
        USER = config.getProperty("db.user", "root");
        PASSWORD = config.getProperty("db.password", "");

        if (URL == null || URL.isBlank()) {
            throw new ExceptionInInitializerError("db.url is required in db.properties");
        }

        try {
            Class.forName(DRIVER);
            initSchema();
            seedUsers();
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DatabaseUtil() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream in = DatabaseUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load db.properties: " + e.getMessage());
        }

        override(props, "db.url", "TASKMANAGER_DB_URL");
        override(props, "db.user", "TASKMANAGER_DB_USER");
        override(props, "db.password", "TASKMANAGER_DB_PASSWORD");
        override(props, "db.driver", "TASKMANAGER_DB_DRIVER");

        return props;
    }

    private static void override(Properties props, String key, String envKey) {
        String value = System.getenv(envKey);
        if (value != null && !value.isBlank()) {
            props.setProperty(key, value);
        }
    }

    private static void initSchema() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password_hash VARCHAR(64) NOT NULL,
                    full_name VARCHAR(100) NOT NULL,
                    role VARCHAR(20) NOT NULL CHECK (role IN ('manager', 'employee')),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    description VARCHAR(2000) DEFAULT '',
                    status VARCHAR(20) NOT NULL DEFAULT 'assigned',
                    priority VARCHAR(20) NOT NULL DEFAULT 'medium',
                    due_date DATE NULL,
                    created_by INT NOT NULL,
                    assigned_to INT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (created_by) REFERENCES users(id),
                    FOREIGN KEY (assigned_to) REFERENCES users(id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            migrateTasksTable(stmt);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to connect to MySQL. Create the database first (see sql/schema.sql): "
                            + e.getMessage(),
                    e);
        }
    }

    private static void migrateTasksTable(Statement stmt) {
        try {
            stmt.execute("ALTER TABLE tasks ADD COLUMN created_by INT");
        } catch (SQLException ignored) { }
        try {
            stmt.execute("ALTER TABLE tasks ADD COLUMN assigned_to INT");
        } catch (SQLException ignored) { }
    }

    private static void seedUsers() {
        try {
            UserDAO userDAO = new UserDAO();
            if (userDAO.count() > 0) {
                return;
            }
            insertUser(userDAO, "manager", "manager123", "Admin Manager", "manager");
            insertUser(userDAO, "employee1", "emp123", "John Smith", "employee");
            insertUser(userDAO, "employee2", "emp123", "Jane Doe", "employee");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to seed users", e);
        }
    }

    private static void insertUser(UserDAO dao, String username, String password, String fullName, String role)
            throws SQLException {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setFullName(fullName);
        user.setRole(role);
        dao.insert(user);
    }
}
