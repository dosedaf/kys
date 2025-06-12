package src.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import src.controller.CategoryController;
import src.controller.TransactionController;
import src.model.Category;

public class CategoryManagementDialog extends JDialog {
    private CategoryController categoryController;
    private TransactionController transactionController;
    private JTable categoryTable;
    private CategoryTableModel categoryTableModel;

    public CategoryManagementDialog(Frame owner, CategoryController catCtrl, TransactionController transCtrl) {
        super(owner, "Manage Categories", true);
        this.categoryController = catCtrl;
        this.transactionController = transCtrl;
        initComponents();
        loadCategories();
        setSize(500, 400);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        categoryTableModel = new CategoryTableModel();
        categoryTable = new JTable(categoryTableModel);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(categoryTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");
        JButton closeButton = new JButton("Close");

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addCategory());
        deleteButton.addActionListener(e -> deleteCategory());
        closeButton.addActionListener(e -> setVisible(false));
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryController.getCategories();
            categoryTableModel.setCategories(categories);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCategory() {
        JTextField nameField = new JTextField();
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"INCOME_CATEGORY", "EXPENSE_CATEGORY"});
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Category Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Category Type:"));
        panel.add(typeComboBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Category", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Category name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String type = (String) typeComboBox.getSelectedItem();
            try {
                categoryController.addCategory(new Category(name, "", type));
                loadCategories();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error adding category: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a category to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Category selectedCategory = categoryTableModel.getCategoryAt(selectedRow);

        try {
            if (transactionController.hasTransactionsForCategory(selectedCategory.getId())) {
                JOptionPane.showMessageDialog(this,
                        "This category cannot be deleted because it has transactions linked to it.",
                        "Deletion Prevented",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirmation = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete category '" + selectedCategory.getName() + "'?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirmation == JOptionPane.YES_OPTION) {
                categoryController.deleteCategory(selectedCategory.getId());
                loadCategories();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}