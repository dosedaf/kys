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
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

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
    private transient TransactionController transactionController;
    private transient AccountController accountController;
    private transient CategoryController categoryController;
    private transient Transaction currentTransaction;
    private transient Runnable refreshCallback;

    private JTextField descriptionField;
    private JFormattedTextField amountField;
    private JTextField dateField;
    private JComboBox<String> typeComboBox;
    private JComboBox<CategoryItem> categoryComboBox;
    private JComboBox<AccountItem> accountComboBox;
    private JButton saveButton;
    private JButton cancelButton;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private static class CategoryItem {
        private final int id;
        private final String name;
        public CategoryItem(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        @Override public String toString() { return name; }
    }

    private static class AccountItem {
        private final int id;
        private final String name;
        public AccountItem(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        @Override public String toString() { return name; }
    }

    public TransactionDialog(Frame owner, TransactionController tCtrl, AccountController aCtrl, CategoryController cCtrl,
                             Transaction transactionToEdit, Runnable onSaveCallback) {
        super(owner, transactionToEdit == null ? "Add Transaction" : "Edit Transaction", true);
        this.transactionController = tCtrl;
        this.accountController = aCtrl;
        this.categoryController = cCtrl;
        this.currentTransaction = transactionToEdit;
        this.refreshCallback = onSaveCallback;

        // CRITICAL ORDER:
        // 1. Create the UI components first.
        initComponents();
        // 2. Then, populate them with data.
        populateComboBoxes();

        if (this.currentTransaction != null) {
            populateFields();
        }
        pack();
        setMinimumSize(new Dimension(450, 0));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        descriptionField = new JTextField(25);
        formPanel.add(descriptionField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("id", "ID"));
        DecimalFormat amountFormat = new DecimalFormat("###,##0.00", symbols);
        NumberFormatter amountFormatter = new NumberFormatter(amountFormat);
        amountFormatter.setValueClass(BigDecimal.class);
        amountFormatter.setAllowsInvalid(false);
        amountFormatter.setMinimum(BigDecimal.ZERO);
        amountField = new JFormattedTextField(amountFormatter);
        amountField.setColumns(15);
        amountField.setValue(BigDecimal.ZERO);
        formPanel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        dateField = new JTextField(15);
        if (currentTransaction == null) {
            dateField.setText(DATE_FORMATTER.format(LocalDate.now()));
        }
        formPanel.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        typeComboBox = new JComboBox<>(new String[]{"EXPENSE", "INCOME"});
        formPanel.add(typeComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Category:"), gbc);
        JPanel categoryPanel = new JPanel(new BorderLayout(5, 0));
        categoryComboBox = new JComboBox<>();
        JButton addCategoryButton = new JButton("+");
        categoryPanel.add(categoryComboBox, BorderLayout.CENTER);
        categoryPanel.add(addCategoryButton, BorderLayout.EAST);
        gbc.gridx = 1; gbc.gridy = 4;
        formPanel.add(categoryPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(new JLabel("Account:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        accountComboBox = new JComboBox<>();
        formPanel.add(accountComboBox, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> saveTransaction());
        cancelButton.addActionListener(e -> setVisible(false));
        addCategoryButton.addActionListener(e -> addNewCategory());
    }

    private void populateComboBoxes() {
        refreshCategoryComboBox(null);

        try {
            List<Account> accounts = accountController.getAccounts();
            accountComboBox.removeAllItems();
            for (Account acc : accounts) {
                accountComboBox.addItem(new AccountItem(acc.getId(), acc.getName()));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading accounts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        typeComboBox.addActionListener(e -> refreshCategoryComboBox(null));
    }

    private void populateFields() {
        if (currentTransaction == null) return;
        descriptionField.setText(currentTransaction.getDescription());
        amountField.setValue(currentTransaction.getAmount());
        dateField.setText(DATE_FORMATTER.format(currentTransaction.getDate()));
        typeComboBox.setSelectedItem(currentTransaction.getType().toUpperCase());
        refreshCategoryComboBox(currentTransaction.getCategoryName());

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
            amountField.commitEdit();
            amount = (BigDecimal) amountField.getValue();
            if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Amount must be a non-negative number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                amountField.requestFocusInWindow();
                return;
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
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
            if (currentTransaction == null) {
                Transaction newTransaction = new Transaction(description, amount, date, type, selectedCategoryItem.getId(), selectedAccountItem.getId());
                transactionController.addTransaction(newTransaction);
                JOptionPane.showMessageDialog(this, "Transaction added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                currentTransaction.setDescription(description);
                currentTransaction.setAmount(amount);
                currentTransaction.setDate(date);
                currentTransaction.setType(type);
                currentTransaction.setCategoryId(selectedCategoryItem.getId());
                currentTransaction.setAccountId(selectedAccountItem.getId());
                transactionController.updateTransaction(currentTransaction);
                JOptionPane.showMessageDialog(this, "Transaction updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            if (refreshCallback != null) {
                refreshCallback.run();
            }
            setVisible(false);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving transaction: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addNewCategory() {
        String categoryName = JOptionPane.showInputDialog(this, "Enter new category name:", "Add Category", JOptionPane.PLAIN_MESSAGE);
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return;
        }
        String type = (String) typeComboBox.getSelectedItem();
        String categoryType = type.equals("INCOME") ? "INCOME_CATEGORY" : "EXPENSE_CATEGORY";
        Category newCategory = new Category(categoryName.trim(), "", categoryType);
        try {
            categoryController.addCategory(newCategory);
            refreshCategoryComboBox(newCategory.getName());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding category: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshCategoryComboBox(String categoryNameToSelect) {
        try {
            List<Category> categories = categoryController.getCategories();
            categoryComboBox.removeAllItems();
            CategoryItem itemToSelect = null;
            String selectedTxnType = typeComboBox.getSelectedItem().toString();

            for (Category cat : categories) {
                String catType = cat.getType();
                boolean typeMatches = (selectedTxnType.equals("EXPENSE") && catType.equals("EXPENSE_CATEGORY")) ||
                                      (selectedTxnType.equals("INCOME") && catType.equals("INCOME_CATEGORY"));
                if (typeMatches) {
                    CategoryItem item = new CategoryItem(cat.getId(), cat.getName());
                    categoryComboBox.addItem(item);
                    if (cat.getName().equals(categoryNameToSelect)) {
                        itemToSelect = item;
                    }
                }
            }

            if (itemToSelect != null) {
                categoryComboBox.setSelectedItem(itemToSelect);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error reloading categories: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}