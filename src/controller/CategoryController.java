package src.controller;

import java.sql.SQLException;
import java.util.List; // Required for return type

import src.dao.CategoryDAO;
import src.model.Category;
// No need to import ArrayList if only used for return type inference in catch

public class CategoryController {
    private CategoryDAO dao;

    public CategoryController() {
        dao = new CategoryDAO();
    }

    /**
     * Adds a new category.
     * @param c The category to add.
     * @throws SQLException if a database error occurs.
     */
    public void addCategory(Category c) throws SQLException {
        dao.insert(c);
    }

    /**
     * Retrieves all categories.
     * @return A list of all categories.
     * @throws SQLException if a database error occurs.
     */
    public List<Category> getCategories() throws SQLException {
        return dao.getAll(); // Corrected to return the list
    }

    /**
     * Updates an existing category.
     * @param c The category to update.
     * @throws SQLException if a database error occurs.
     */
    public void updateCategory(Category c) throws SQLException {
        dao.update(c);
    }

    /**
     * Deletes a category by its ID.
     * @param id The ID of the category to delete.
     * @throws SQLException if a database error occurs.
     */
    public void deleteCategory(int id) throws SQLException {
        dao.delete(id);
    }
}