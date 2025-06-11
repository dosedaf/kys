package src.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import src.model.Transaction;

public class TransactionDAO {

    public void insert(Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions (description, amount, date, type, category_id, account_id) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, t.getDescription());
            stmt.setBigDecimal(2, t.getAmount());
            stmt.setDate(3, Date.valueOf(t.getDate()));
            stmt.setString(4, t.getType());
            stmt.setInt(5, t.getCategoryId());
            stmt.setInt(6, t.getAccountId());
            stmt.executeUpdate();
        }
    }

    public List<Transaction> getAll() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.id, t.description, t.amount, t.date, t.type, " +
                     "t.category_id, c.name as category_name, " +
                     "t.account_id, a.name as account_name " +
                     "FROM transactions t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "LEFT JOIN accounts a ON t.account_id = a.id " +
                     "ORDER BY t.date DESC, t.id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transaction t = new Transaction(
                        rs.getInt("id"),
                        rs.getString("description"),
                        rs.getBigDecimal("amount"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("type"),
                        rs.getInt("category_id"),
                        rs.getInt("account_id"),
                        rs.getString("category_name"), // Now populated
                        rs.getString("account_name")   // Now populated
                );
                list.add(t);
            }
        }
        return list;
    }

    public Transaction getById(int id) throws SQLException {
        String sql = "SELECT t.id, t.description, t.amount, t.date, t.type, " +
                     "t.category_id, c.name as category_name, " +
                     "t.account_id, a.name as account_name " +
                     "FROM transactions t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "LEFT JOIN accounts a ON t.account_id = a.id " +
                     "WHERE t.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Transaction(
                            rs.getInt("id"),
                            rs.getString("description"),
                            rs.getBigDecimal("amount"),
                            rs.getDate("date").toLocalDate(),
                            rs.getString("type"),
                            rs.getInt("category_id"),
                            rs.getInt("account_id"),
                            rs.getString("category_name"),
                            rs.getString("account_name")
                    );
                }
            }
        }
        return null;
    }


    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void update(Transaction t) throws SQLException {
        String sql = "UPDATE transactions SET description=?, amount=?, date=?, type=?, category_id=?, account_id=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getDescription());
            stmt.setBigDecimal(2, t.getAmount());
            stmt.setDate(3, Date.valueOf(t.getDate()));
            stmt.setString(4, t.getType());
            stmt.setInt(5, t.getCategoryId());
            stmt.setInt(6, t.getAccountId());
            stmt.setInt(7, t.getId());
            stmt.executeUpdate();
        }
    }

    public Transaction getById(int id, Connection conn) throws SQLException {
         String sql = "SELECT t.id, t.description, t.amount, t.date, t.type, " +
                     "t.category_id, c.name as category_name, " +
                     "t.account_id, a.name as account_name " +
                     "FROM transactions t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "LEFT JOIN accounts a ON t.account_id = a.id " +
                     "WHERE t.id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Transaction(
                            rs.getInt("id"),
                            rs.getString("description"),
                            rs.getBigDecimal("amount"),
                            rs.getDate("date").toLocalDate(),
                            rs.getString("type"),
                            rs.getInt("category_id"),
                            rs.getInt("account_id"),
                            rs.getString("category_name"),
                            rs.getString("account_name")
                    );
                }
            }
        }
        return null;
    }

    public void insertTransactionAndUpdateAccount(Transaction t, AccountDAO accountDAO, Connection conn) throws SQLException {
        String sqlInsert = "INSERT INTO transactions (description, amount, date, type, category_id, account_id) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, t.getDescription());
            stmt.setBigDecimal(2, t.getAmount());
            stmt.setDate(3, Date.valueOf(t.getDate()));
            stmt.setString(4, t.getType());
            stmt.setInt(5, t.getCategoryId());
            stmt.setInt(6, t.getAccountId());
            stmt.executeUpdate();
        }

        BigDecimal amountChange = t.getAmount();
        if ("Expense".equalsIgnoreCase(t.getType())) {
            amountChange = amountChange.negate(); 
        }
        accountDAO.adjustBalance(t.getAccountId(), amountChange, conn);
    }

    public void deleteTransactionAndUpdateAccount(int transactionId, AccountDAO accountDAO, Connection conn) throws SQLException {
        Transaction t = this.getById(transactionId, conn);
        if (t == null) {
            throw new SQLException("Transaction with ID " + transactionId + " not found for deletion.");
        }

        String sqlDelete = "DELETE FROM transactions WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlDelete)) {
            stmt.setInt(1, transactionId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting transaction failed, no rows affected. ID: " + transactionId);
            }
        }

        BigDecimal balanceAdjustment;
        if ("Expense".equalsIgnoreCase(t.getType())) {
            balanceAdjustment = t.getAmount();
        } else { 
            balanceAdjustment = t.getAmount().negate();
        }
        accountDAO.adjustBalance(t.getAccountId(), balanceAdjustment, conn);
    }

    public void updateTransactionAndUpdateAccounts(Transaction oldTransactionData, Transaction newTransaction, AccountDAO accountDAO, Connection conn) throws SQLException {
        BigDecimal oldAmountReversion;
        if ("Expense".equalsIgnoreCase(oldTransactionData.getType())) {
            oldAmountReversion = oldTransactionData.getAmount(); 
        } else { 
            oldAmountReversion = oldTransactionData.getAmount().negate(); 
        }
        accountDAO.adjustBalance(oldTransactionData.getAccountId(), oldAmountReversion, conn);

        BigDecimal newAmountImpact = newTransaction.getAmount();
        if ("Expense".equalsIgnoreCase(newTransaction.getType())) {
            newAmountImpact = newAmountImpact.negate(); 
        }
        accountDAO.adjustBalance(newTransaction.getAccountId(), newAmountImpact, conn);

        String sqlUpdate = "UPDATE transactions SET description=?, amount=?, date=?, type=?, category_id=?, account_id=? WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            stmt.setString(1, newTransaction.getDescription());
            stmt.setBigDecimal(2, newTransaction.getAmount());
            stmt.setDate(3, Date.valueOf(newTransaction.getDate()));
            stmt.setString(4, newTransaction.getType());
            stmt.setInt(5, newTransaction.getCategoryId());
            stmt.setInt(6, newTransaction.getAccountId());
            stmt.setInt(7, newTransaction.getId()); 
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                 throw new SQLException("Updating transaction failed, no rows affected. ID: " + newTransaction.getId());
            }
        }
    }
}