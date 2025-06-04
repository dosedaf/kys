package src.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.math.BigDecimal; // For TableRowSorter comparator
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator; // For sorting BigDecimal in TableRowSorter
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

import src.controller.AccountController;
import src.controller.CategoryController;
import src.controller.TransactionController;
import src.model.Account;
import src.model.Category;
import src.model.Transaction;

public class TransactionPanel extends JPanel {

    private TransactionController transactionController;
    private AccountController accountController;
    private CategoryController categoryController;

    private JTable transactionTable;
    private TransactionTableModel transactionTableModel;

    private JComboBox<Account> accountFilterComboBox;
    private JComboBox<Category> categoryFilterComboBox;
    private JComboBox<String> typeFilterComboBox;

    private JButton addButton, editButton, deleteButton;
    private JButton applyFiltersButton, clearFiltersButton;

    public TransactionPanel(TransactionController transactionController,
                            AccountController accountController,
                            CategoryController categoryController) {
        this.transactionController = transactionController;
        this.accountController = accountController;
        this.categoryController = categoryController;

        setLayout(new BorderLayout(10, 10)); // Add some spacing
        setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding around the panel
        setBackground(Color.WHITE); // Monochrome theme

        initComponents();
        loadInitialData();
    }

    private void initComponents() {
        // --- Filter Panel ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(Color.WHITE);

        filterPanel.add(new JLabel("Account:"));
        accountFilterComboBox = new JComboBox<>();
        filterPanel.add(accountFilterComboBox);

        filterPanel.add(new JLabel("Category:"));
        categoryFilterComboBox = new JComboBox<>();
        filterPanel.add(categoryFilterComboBox);

        filterPanel.add(new JLabel("Type:"));
        typeFilterComboBox = new JComboBox<>(new String[]{"All", "Income", "Expense"});
        filterPanel.add(typeFilterComboBox);

        applyFiltersButton = new JButton("Apply Filters");
        styleButton(applyFiltersButton);
        filterPanel.add(applyFiltersButton);

        clearFiltersButton = new JButton("Clear Filters");
        styleButton(clearFiltersButton);
        filterPanel.add(clearFiltersButton);

        add(filterPanel, BorderLayout.NORTH);

        // --- Transaction Table ---
        transactionTableModel = new TransactionTableModel();
        transactionTable = new JTable(transactionTableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setFillsViewportHeight(true); // Makes the table fill the scroll pane's height
        transactionTable.setBackground(Color.WHITE);
        transactionTable.getTableHeader().setBackground(Color.LIGHT_GRAY); // Header background
        transactionTable.getTableHeader().setForeground(Color.BLACK);     // Header text

        // Setup sorter for the table
        TableRowSorter<TransactionTableModel> sorter = new TableRowSorter<>(transactionTableModel);
        // Provide a comparator for BigDecimal if you want to sort the Amount column correctly
        sorter.setComparator(3, Comparator.comparing(v -> (BigDecimal) v, BigDecimal::compareTo)); // Column 3 is 'Amount'
        transactionTable.setRowSorter(sorter);


        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        addButton = new JButton("Add New Transaction");
        styleButton(addButton);
        buttonPanel.add(addButton);

        editButton = new JButton("Edit Selected Transaction");
        styleButton(editButton);
        buttonPanel.add(editButton);

        deleteButton = new JButton("Delete Selected Transaction");
        styleButton(deleteButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        addButton.addActionListener(e -> openTransactionDialog(null)); // null for new transaction

        editButton.addActionListener(e -> {
            int selectedRow = transactionTable.getSelectedRow();
            if (selectedRow >= 0) {
                // Convert view row index to model row index if table is sorted/filtered
                int modelRow = transactionTable.convertRowIndexToModel(selectedRow);
                Transaction transactionToEdit = transactionTableModel.getTransactionAt(modelRow);
                if (transactionToEdit != null) {
                    openTransactionDialog(transactionToEdit);
                } else {
                    JOptionPane.showMessageDialog(this, "Could not retrieve transaction details for editing.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a transaction to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> deleteSelectedTransaction());

        applyFiltersButton.addActionListener(e -> loadTransactionsWithFilters());
        clearFiltersButton.addActionListener(e -> {
            accountFilterComboBox.setSelectedIndex(0); // Assuming index 0 is "All" or default
            categoryFilterComboBox.setSelectedIndex(0); // Assuming index 0 is "All" or default
            typeFilterComboBox.setSelectedIndex(0); // "All"
            loadTransactionsWithFilters(); // Reload with default "All" filters
        });
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(220, 220, 220)); // Light gray
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        // button.setBorder(new LineBorder(Color.GRAY)); // Optional border
    }


    private void loadInitialData() {
        loadFilterComboBoxes();
        loadTransactionsWithFilters(); // Initial load (effectively "all" if filters are at default)
    }

    private void loadFilterComboBoxes() {
        try {
            // Accounts
            List<Account> accounts = accountController.getAccounts();
            accountFilterComboBox.addItem(null); // Represents "All Accounts"
            for (Account acc : accounts) {
                accountFilterComboBox.addItem(acc);
            }
            // Custom renderer to display account name, but store Account object
            accountFilterComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Account) {
                        setText(((Account) value).getName());
                    } else if (value == null) {
                        setText("All Accounts");
                    }
                    return this;
                }
            });

            // Categories
            List<Category> categories = categoryController.getCategories();
            categoryFilterComboBox.addItem(null); // Represents "All Categories"
            for (Category cat : categories) {
                categoryFilterComboBox.addItem(cat);
            }
            // Custom renderer to display category name
            categoryFilterComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Category) {
                        setText(((Category) value).getName());
                    } else if (value == null) {
                        setText("All Categories");
                    }
                    return this;
                }
            });

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading filter options: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void loadTransactionsWithFilters() {
        // TODO: Implement actual filtering logic in TransactionController
        // For now, this just reloads all transactions.
        // The controller method would need to accept filter parameters.
        try {
            // Placeholder: Get selected filter values
            Account selectedAccount = (Account) accountFilterComboBox.getSelectedItem();
            Category selectedCategory = (Category) categoryFilterComboBox.getSelectedItem();
            String selectedType = (String) typeFilterComboBox.getSelectedItem();

            // This is where you'd call a more sophisticated method in your TransactionController
            // e.g., transactionController.getFilteredTransactions(selectedAccount, selectedCategory, selectedType)
            // For now, we just call getTransactions() which fetches all.
            // You'll need to enhance TransactionController and TransactionDAO to handle filtering.
            List<Transaction> transactions;
            // if (selectedAccount != null || selectedCategory != null || !"All".equals(selectedType)) {
            //    // Call a filtered get method in controller
            //    transactions = transactionController.getFilteredTransactions(
            //            selectedAccount != null ? selectedAccount.getId() : null,
            //            selectedCategory != null ? selectedCategory.getId() : null,
            //            !"All".equals(selectedType) ? selectedType : null
            //    );
            // } else {
                transactions = transactionController.getTransactions();
            // }
            transactionTableModel.setTransactions(transactions);
        } catch (SQLException e) {
            transactionTableModel.setTransactions(new ArrayList<>()); // Clear table on error
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openTransactionDialog(Transaction transaction) {
        // We will create TransactionDialog.java next.
        // It will take the controllers and the transaction (if editing)
        TransactionDialog dialog = new TransactionDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), // Parent frame
            transactionController,
            accountController,
            categoryController,
            transaction // null for new, existing object for edit
        );
        dialog.setVisible(true);

        // After the dialog is closed, refresh the transaction list
        loadTransactionsWithFilters();
    }

    private void deleteSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = transactionTable.convertRowIndexToModel(selectedRow);
            Transaction transactionToDelete = transactionTableModel.getTransactionAt(modelRow);
            if (transactionToDelete != null) {
                int confirmation = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete transaction: '" + transactionToDelete.getDescription() + "'?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirmation == JOptionPane.YES_OPTION) {
                    try {
                        transactionController.deleteTransaction(transactionToDelete.getId());
                        JOptionPane.showMessageDialog(this, "Transaction deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadTransactionsWithFilters(); // Refresh list
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(this, "Error deleting transaction: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
}