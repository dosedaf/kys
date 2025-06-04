package src.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List; // Ensure this import is present

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import src.controller.AccountController;
import src.controller.CategoryController;
import src.controller.TransactionController;
import src.model.Account;
import src.model.Category;
import src.model.Transaction;

public class TransactionDialog extends JDialog {
    private transient TransactionController transactionController; // transient to avoid potential serialization issues if ever serialized
    private transient AccountController accountController;
    private transient CategoryController categoryController;
    private transient Transaction currentTransaction; // The transaction being edited, or null if adding
    private transient Runnable refreshCallback; // Callback to refresh data in the parent view

    private JTextField descriptionField;
    private JFormattedTextField amountField;
    private JTextField dateField;
    private JComboBox<String> typeComboBox;
    private JComboBox<CategoryItem> categoryComboBox;
    private JComboBox<AccountItem> accountComboBox;
    private JButton saveButton;
    private JButton cancelButton; // Added cancelButton variable

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

    // Wrapper class for displaying Category objects in JComboBox
    private static class CategoryItem {
        private final int id;
        private final String name;

        public CategoryItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // Wrapper class for displaying Account objects in JComboBox
    private static class AccountItem {
        private final int id;
        private final String name;

        public AccountItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public TransactionDialog(Frame owner, TransactionController tCtrl, AccountController aCtrl, CategoryController cCtrl,
                             Transaction transactionToEdit, Runnable onSaveCallback) {
        super(owner, transactionToEdit == null ? "Add Transaction" : "Edit Transaction", true); // Modal
        this.transactionController = tCtrl;
        this.accountController = aCtrl;
        this.categoryController = cCtrl;
        this.currentTransaction = transactionToEdit;
        this.refreshCallback = onSaveCallback;

        initComponents();
        populateComboBoxes();
        if (this.currentTransaction != null) {
            populateFields();
        }
        pack(); // Adjust dialog size to components
        setMinimumSize(new Dimension(450, 0)); // Ensure a minimum width
        setLocationRelativeTo(owner); // Center on owner
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // Gaps between components

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Spacing between components
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Description
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; // Allow field to expand
        descriptionField = new JTextField(25);
        formPanel.add(descriptionField, gbc);

        // Amount
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        NumberFormat amountFormat = DecimalFormat.getNumberInstance();
        amountFormat.setMinimumFractionDigits(2);
        amountFormat.setMaximumFractionDigits(2);
        amountFormat.setGroupingUsed(false); // Avoid commas like 1,000.00 for easier parsing if needed

        NumberFormatter amountFormatter = new NumberFormatter(amountFormat);
        amountFormatter.setValueClass(BigDecimal.class);
        amountFormatter.setAllowsInvalid(false); // Prevents user from typing invalid chars directly
        amountFormatter.setMinimum(BigDecimal.ZERO); // Amount must be non-negative

        amountField = new JFormattedTextField(amountFormatter);
        amountField.setColumns(15);
        amountField.setValue(BigDecimal.ZERO); // Default value
        formPanel.add(amountField, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        dateField = new JTextField(15);
        if (currentTransaction == null) { // Default to today for new transactions
            dateField.setText(DATE_FORMATTER.format(LocalDate.now()));
        }
        formPanel.add(dateField, gbc);

        // Type
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        typeComboBox = new JComboBox<>(new String[]{"EXPENSE", "INCOME"}); // Match DB ENUM
        formPanel.add(typeComboBox, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        categoryComboBox = new JComboBox<>();
        formPanel.add(categoryComboBox, gbc);

        // Account
        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(new JLabel("Account:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        accountComboBox = new JComboBox<>();
        formPanel.add(accountComboBox, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel"); // Initialize here
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        saveButton.addActionListener(e -> saveTransaction());
        cancelButton.addActionListener(e -> setVisible(false)); // Close dialog on cancel
    }

    private void populateComboBoxes() {
        try {
            // Populate Categories ComboBox
            List<Category> categories = categoryController.getCategories();
            categoryComboBox.removeAllItems(); // Clear previous items
            String selectedType = typeComboBox.getSelectedItem() != null ? typeComboBox.getSelectedItem().toString() : "EXPENSE";
            // Filter categories based on selected transaction type (Expense/Income)
            for (Category cat : categories) {
                // Assuming Category model has getType() like "EXPENSE_CATEGORY" or "INCOME_CATEGORY"
                if ((selectedType.equals("EXPENSE") && cat.getType().equals("EXPENSE_CATEGORY")) ||
                    (selectedType.equals("INCOME") && cat.getType().equals("INCOME_CATEGORY"))) {
                    categoryComboBox.addItem(new CategoryItem(cat.getId(), cat.getName()));
                }
            }

            // Populate Accounts ComboBox
            List<Account> accounts = accountController.getAccounts();
            accountComboBox.removeAllItems(); // Clear previous items
            for (Account acc : accounts) {
                accountComboBox.addItem(new AccountItem(acc.getId(), acc.getName()));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories/accounts: " + e.getMessage(),
                    "Initialization Error", JOptionPane.ERROR_MESSAGE);
        }

        // Add action listener to typeComboBox to re-filter categories when type changes
        typeComboBox.addActionListener(e -> {
            String selectedTxnType = typeComboBox.getSelectedItem().toString();
            try {
                List<Category> categories = categoryController.getCategories();
                categoryComboBox.removeAllItems();
                for (Category cat : categories) {
                    if ((selectedTxnType.equals("EXPENSE") && cat.getType().equals("EXPENSE_CATEGORY")) ||
                        (selectedTxnType.equals("INCOME") && cat.getType().equals("INCOME_CATEGORY"))) {
                        categoryComboBox.addItem(new CategoryItem(cat.getId(), cat.getName()));
                    }
                }
                // Reselect if currentTransaction is being edited and matches new filter
                if (currentTransaction != null && categoryComboBox.getItemCount() > 0) {
                     for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                        if (categoryComboBox.getItemAt(i).getId() == currentTransaction.getCategoryId()) {
                            CategoryItem item = categoryComboBox.getItemAt(i);
                            // Check if this category still matches the selected transaction type
                            Category originalCat = categories.stream().filter(c -> c.getId() == item.getId()).findFirst().orElse(null);
                            if(originalCat != null && 
                               ((selectedTxnType.equals("EXPENSE") && originalCat.getType().equals("EXPENSE_CATEGORY")) ||
                                (selectedTxnType.equals("INCOME") && originalCat.getType().equals("INCOME_CATEGORY")))) {
                                categoryComboBox.setSelectedIndex(i);
                            }
                            break;
                        }
                    }
                }


            } catch (SQLException ex) {
                 JOptionPane.showMessageDialog(this, "Error reloading categories: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void populateFields() {
        if (currentTransaction == null) return;

        descriptionField.setText(currentTransaction.getDescription());
        amountField.setValue(currentTransaction.getAmount());
        dateField.setText(DATE_FORMATTER.format(currentTransaction.getDate()));
        typeComboBox.setSelectedItem(currentTransaction.getType().toUpperCase()); // Ensure case matches JComboBox items

        // Trigger category re-filter based on the transaction's type
        // typeComboBox.getActionListeners()[typeComboBox.getActionListeners().length -1].actionPerformed(null); // This is a bit hacky
        // A cleaner way is to just call the filtering logic again:
        try {
            String selectedTxnType = currentTransaction.getType().toUpperCase();
            List<Category> categories = categoryController.getCategories();
            categoryComboBox.removeAllItems();
            for (Category cat : categories) {
                if ((selectedTxnType.equals("EXPENSE") && cat.getType().equals("EXPENSE_CATEGORY")) ||
                    (selectedTxnType.equals("INCOME") && cat.getType().equals("INCOME_CATEGORY"))) {
                    categoryComboBox.addItem(new CategoryItem(cat.getId(), cat.getName()));
                }
            }
        } catch (SQLException ex) {
             JOptionPane.showMessageDialog(this, "Error reloading categories for edit: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }


        // Select category in ComboBox
        for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
            if (categoryComboBox.getItemAt(i).getId() == currentTransaction.getCategoryId()) {
                categoryComboBox.setSelectedIndex(i);
                break;
            }
        }
        // Select account in ComboBox
        for (int i = 0; i < accountComboBox.getItemCount(); i++) {
            if (accountComboBox.getItemAt(i).getId() == currentTransaction.getAccountId()) {
                accountComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private void saveTransaction() {
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Description cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            descriptionField.requestFocusInWindow();
            return;
        }

        BigDecimal amount;
        try {
            amountField.commitEdit(); // Ensure current text is parsed
            amount = (BigDecimal) amountField.getValue();
            if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) { // Amount must be non-negative
                JOptionPane.showMessageDialog(this, "Amount must be a non-negative number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                amountField.requestFocusInWindow();
                return;
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount format. Please enter a valid number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            amountField.requestFocusInWindow();
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText().trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            dateField.requestFocusInWindow();
            return;
        }

        String type = (String) typeComboBox.getSelectedItem();
        CategoryItem selectedCategoryItem = (CategoryItem) categoryComboBox.getSelectedItem();
        AccountItem selectedAccountItem = (AccountItem) accountComboBox.getSelectedItem();

        if (selectedCategoryItem == null) {
            JOptionPane.showMessageDialog(this, "Please select a category.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            categoryComboBox.requestFocusInWindow();
            return;
        }
        if (selectedAccountItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an account.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            accountComboBox.requestFocusInWindow();
            return;
        }

        try {
            if (currentTransaction == null) { // Adding a new transaction
                // This is where the constructor from Error 1 is called
                Transaction newTransaction = new Transaction(
                        description, amount, date, type,
                        selectedCategoryItem.getId(), selectedAccountItem.getId()
                );
                transactionController.addTransaction(newTransaction);
                JOptionPane.showMessageDialog(this, "Transaction added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else { // Updating an existing transaction
                currentTransaction.setDescription(description);
                currentTransaction.setAmount(amount);
                currentTransaction.setDate(date);
                currentTransaction.setType(type);
                currentTransaction.setCategoryId(selectedCategoryItem.getId());
                currentTransaction.setAccountId(selectedAccountItem.getId());
                // categoryName and accountName are not directly set here as they are for display from DB.
                // The backend controller handles updating the core transaction fields.
                transactionController.updateTransaction(currentTransaction);
                JOptionPane.showMessageDialog(this, "Transaction updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            if (refreshCallback != null) {
                refreshCallback.run(); // Call the refresh method on the parent panel
            }
            setVisible(false); // Close the dialog
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving transaction: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) { // Catch any other unexpected error during save
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Print stack trace for debugging
        }
    }
} // This should be the final closing brace of the class.