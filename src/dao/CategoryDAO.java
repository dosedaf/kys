package src.dao;

import src.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
   public void insert(Category c) throws SQLException {
       String sql = "INSERT INTO categories (name, description, type) VALUES (?,?,?)";
       try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
           stmt.setString(1, c.getName());
           stmt.setString(2, c.getDescription());
           stmt.setString(3, c.getType());
           stmt.executeUpdate();
       }
   } 
   
   public List<Category> getAll() throws SQLException {
    List<Category> list = new ArrayList<>();
    String sql = "SELECT * FROM categories ORDER BY id asc";
    try (Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            Category t = new Category(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("type"));
            list.add(t);
        }
    }

    return list;
   }
   
   public void delete(int id) throws SQLException {
       String sql = "DELETE FROM categories WHERE id=?";
       try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
           stmt.setInt(1, id);
           stmt.executeUpdate();
       }
   }
   
   public void update(Category c) throws SQLException {
       String sql = "UPDATE categories SET name=?, description=?, type=? WHERE id=?";
       try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
           stmt.setString(1, c.getName());
           stmt.setString(2, c.getDescription());
           stmt.setString(3, c.getType());
           stmt.setInt(4, c.getId());
           stmt.executeUpdate();
       }
   } 
}
