package src.dao;

import src.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
   public void insert(Transaction t) throws SQLException {
       String sql = "INSERT INTO transactions (description, amount, date, type, category_id, account_id) VALUES (?,?,?,?,?,?)";
       try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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
    String sql = "SELECT * FROM transactions ORDER BY date desc";
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
                    rs.getInt("account_id"));
            list.add(t);
        }
    }
    
    return list;
   }
   
   public Transaction getById(int id) throws SQLException {
    String sql = "SELECT * FROM transactions WHERE id = ?";
    try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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
                        rs.getInt("account_id")
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
}
