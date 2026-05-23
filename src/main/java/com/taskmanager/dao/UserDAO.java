package com.taskmanager.dao;

import com.taskmanager.model.User;
import com.taskmanager.util.DatabaseUtil;
import com.taskmanager.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, full_name, role, password_hash, email, status, otp, otp_expiration FROM users WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT id, username, full_name, role, password_hash, email, status, otp, otp_expiration FROM users WHERE email = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public User authenticate(String username, String password) throws SQLException {
        User user = findByUsername(username);
        if (user != null && PasswordUtil.verify(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public List<User> findEmployees() throws SQLException {
        String sql = "SELECT id, username, full_name, role, password_hash, email, status, otp, otp_expiration FROM users WHERE role = 'employee' AND status != 'pending_verification' ORDER BY full_name";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        }
        return users;
    }

    public List<User> findEmployeesWithTaskCount() throws SQLException {
        return findEmployeesWithTaskCount(null);
    }

    public List<User> findEmployeesWithTaskCount(String search) throws SQLException {
        String sql = "SELECT u.id, u.username, u.full_name, u.role, u.password_hash, u.email, u.status, u.otp, u.otp_expiration, " +
                     "(SELECT COUNT(*) FROM task_assignments ta WHERE ta.user_id = u.id) as task_count " +
                     "FROM users u WHERE u.role = 'employee' AND u.status != 'pending_verification' ";
        if (search != null && !search.isEmpty()) {
            sql += "AND (u.full_name LIKE ? OR u.email LIKE ? OR u.username LIKE ?) ";
        }
        sql += "ORDER BY u.full_name";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (search != null && !search.isEmpty()) {
                String searchPattern = "%" + search + "%";
                ps.setString(1, searchPattern);
                ps.setString(2, searchPattern);
                ps.setString(3, searchPattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRowWithTaskCount(rs));
                }
            }
        }
        return users;
    }

    public List<User> findByRole(String role) throws SQLException {
        String sql = "SELECT id, username, full_name, role, password_hash, email, status, otp, otp_expiration FROM users WHERE role = ? ORDER BY full_name";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        }
        return users;
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT id, username, full_name, role, password_hash, email, status, otp, otp_expiration FROM users WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public void insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, email, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getStatus() != null ? user.getStatus() : "active");
            ps.executeUpdate();
        }
    }

    public boolean updateStatus(int userId, String status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, email = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getStatus());
            ps.setInt(4, user.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean saveOTP(int userId, String otp, java.sql.Timestamp expiration) throws SQLException {
        String sql = "UPDATE users SET otp = ?, otp_expiration = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, otp);
            ps.setTimestamp(2, expiration);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean clearOTP(int userId) throws SQLException {
        String sql = "UPDATE users SET otp = NULL, otp_expiration = NULL WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ? AND role = 'employee'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public int countEmployees() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE role = 'employee' AND status != 'pending_verification'")) {
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int count() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setStatus(rs.getString("status"));
        user.setOtp(rs.getString("otp"));
        user.setOtpExpiration(rs.getTimestamp("otp_expiration"));
        return user;
    }

    private User mapRowWithTaskCount(ResultSet rs) throws SQLException {
        User user = mapRow(rs);
        user.setTaskCount(rs.getInt("task_count"));
        return user;
    }
}
