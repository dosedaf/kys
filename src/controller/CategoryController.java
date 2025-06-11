package src.controller;

import java.sql.SQLException;
import java.util.List;
import src.dao.CategoryDAO;
import src.model.Category;

public class CategoryController {
    private CategoryDAO dao;

    public CategoryController() {
        dao = new CategoryDAO();
    }

    public void addCategory(Category c) throws SQLException {
        dao.insert(c);
    }

    public List<Category> getCategories() throws SQLException {
        return dao.getAll(); // Corrected to return the list
    }

    public void updateCategory(Category c) throws SQLException {
        dao.update(c);
    }

    public void deleteCategory(int id) throws SQLException {
        dao.delete(id);
    }
}