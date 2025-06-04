package src.view;

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import src.controller.CategoryController;

public class CategoryManagementDialog extends JDialog {
    public CategoryManagementDialog(Frame owner, CategoryController categoryController) {
        super(owner, "Manage Categories", true); // Modal
        setSize(400, 300);
        setLocationRelativeTo(owner);
        add(new JLabel("Category Management - To be implemented", SwingConstants.CENTER));
        // In a real implementation, you'd have a JTable, buttons for CRUD, etc.
        // similar to the original CategoryPanel idea.
    }
}