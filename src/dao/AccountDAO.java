package src.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import src.model.Account;

public class AccountDAO {
    public void insert(Account a) throws SQLException {
        String sql = "INSERT INTO accounts (name, balance) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, a.getName());
            stmt.setBigDecimal(2, a.getBalance());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    a.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Account> getAll() throws SQLException {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Account a = new Account(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getBigDecimal("balance"));
                list.add(a);
            }
        }
        return list;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM accounts WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void update(Account a) throws SQLException {
        String sql = "UPDATE accounts SET name=?, balance=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, a.getName());
            stmt.setBigDecimal(2, a.getBalance());
            stmt.setInt(3, a.getId());
            stmt.executeUpdate();
        }
    }

    public void adjustBalance(int accountId, BigDecimal amountChange, Connection conn) throws SQLException {
        String selectSql = "SELECT balance FROM accounts WHERE id = ?";
        BigDecimal currentBalance;
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setInt(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    currentBalance = rs.getBigDecimal("balance");
                } else {
                    throw new SQLException("Account not found with ID: " + accountId + " for balance adjustment.");
                }
            }
        }

        BigDecimal newBalance = currentBalance.add(amountChange);

        String updateSql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setBigDecimal(1, newBalance);
            stmt.setInt(2, accountId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to update balance for account ID: " + accountId + ". Account may have been deleted or value unchanged.");
            }
        }
    }

    public Account getById(int id) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getBigDecimal("balance"));
                }
            }
        }
        return null;
    }
}