package src.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date; // For JSpinner Date model
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;

import src.controller.AccountController;
import src.controller.CategoryController;
import src.controller.TransactionController;
import src.model.Account;
import src.model.Category;
import src.model.Transaction;

public class TransactionDialog extends JDialog {

    private TransactionController transactionController;
    private AccountController accountController;
    private CategoryController categoryController;
    private Transaction currentTransaction; // null if adding, existing object if editing
    private Frame parentFrame; // To properly parent other dialogs like CategoryDialog

    private JTextField descriptionField;
    private JTextField amountField;
    private JSpinner dateSpinner;
    private JComboBox<String> typeComboBox;
    private JComboBox<Object> categoryComboBox; // Can hold Category objects and a String "Add New"
    private JComboBox<Account> accountComboBox;
    private JButton saveButton;
    private JButton cancelButton;

    private final String ADD_NEW_CATEGORY_OPTION = "<Add New Category...>";

    public TransactionDialog(Frame owner,
                             TransactionController transactionController,
                             AccountController accountController,
                             CategoryController categoryController,
                             Transaction transactionToEdit) {
        super(owner, true); // true for modal dialog
        this.parentFrame = owner;
        this.transactionController = transactionController;
        this.accountController = accountController;
        this.categoryController = categoryController;
        this.currentTransaction = transactionToEdit;

        setTitle(currentTransaction == null ? "Add New Transaction" : "Edit Transaction");
        // setSize(450, 350); // Adjust size as needed
        setLayout(new BorderLayout(10,10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10)); // Padding
        getContentPane().setBackground(Color.WHITE);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        populateComboBoxes();

        if (currentTransaction != null) {
            populateFieldsForEdit();
        }
        pack(); // Adjust dialog size to fit components
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Description
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2; // Span two columns
        descriptionField = new JTextField(25);
        formPanel.add(descriptionField, gbc);
        gbc.gridwidth = 1; // Reset

        // Amount
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        amountField = new JTextField(15);
        formPanel.add(amountField, gbc);
        gbc.gridwidth = 1;

        // Date
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        // Using JSpinner for date selection
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        formPanel.add(dateSpinner, gbc);
        gbc.gridwidth = 1;

        // Type
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        typeComboBox = new JComboBox<>(new String[]{"Expense", "Income"});
        formPanel.add(typeComboBox, gbc);
        gbc.gridwidth = 1;

        // Category
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        categoryComboBox = new JComboBox<>();
        formPanel.add(categoryComboBox, gbc);
        gbc.gridwidth = 1;

        // Account
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Account:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        accountComboBox = new JComboBox<>();
        formPanel.add(accountComboBox, gbc);
        gbc.gridwidth = 1;

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        saveButton = new JButton("Save");
        styleButton(saveButton);
        cancelButton = new JButton("Cancel");
        styleButton(cancelButton);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add panels to dialog
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        saveButton.addActionListener(e -> saveTransaction());
        cancelButton.addActionListener(e -> dispose()); // Close dialog

        categoryComboBox.addActionListener(e -> {
            if (ADD_NEW_CATEGORY_OPTION.equals(categoryComboBox.getSelectedItem())) {
                handleAddNewCategory();
            }
        });
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(220, 220, 220));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
    }

    private void populateComboBoxes() {
        // Populate Account ComboBox
        try {
            List<Account> accounts = accountController.getAccounts();
            for (Account acc : accounts) {
                accountComboBox.addItem(acc);
            }
            // Custom renderer for Account ComboBox
            accountComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Account) {
                        setText(((Account) value).getName());
                    }
                    return this;
                }
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading accounts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // Populate Category ComboBox
        populateCategoryDropdown();
    }

    private void populateCategoryDropdown() {
        Object selectedItem = categoryComboBox.getSelectedItem(); // Preserve selection if possible

        categoryComboBox.removeAllItems(); // Clear previous items
        try {
            List<Category> categories = categoryController.getCategories();
            for (Category cat : categories) {
                categoryComboBox.addItem(cat);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        categoryComboBox.addItem(ADD_NEW_CATEGORY_OPTION); // Add special option

        // Custom renderer for Category ComboBox
        categoryComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category) {
                    setText(((Category) value).getName());
                } else if (value instanceof String) { // For "<Add New Category...>"
                    setText((String) value);
                }
                return this;
            }
        });

        // Try to re-select the previously selected item if it's still valid
        if (selectedItem instanceof Category) {
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                if (categoryComboBox.getItemAt(i) instanceof Category &&
                    ((Category) categoryComboBox.getItemAt(i)).getId() == ((Category) selectedItem).getId()) {
                    categoryComboBox.setSelectedIndex(i);
                    break;
                }
            }
        } else if (selectedItem == null && categoryComboBox.getItemCount() > 1) {
             // if nothing was selected, and we have categories, select the first actual category
            if(categoryComboBox.getItemAt(0) instanceof Category){
                categoryComboBox.setSelectedIndex(0);
            }
        }
    }


    private void populateFieldsForEdit() {
        descriptionField.setText(currentTransaction.getDescription());
        amountField.setText(currentTransaction.getAmount().toPlainString());

        // Set Date for JSpinner
        LocalDate ld = currentTransaction.getDate();
        if (ld != null) {
            Date date = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
            dateSpinner.setValue(date);
        }

        typeComboBox.setSelectedItem(currentTransaction.getType());

        // Select Account in ComboBox
        for (int i = 0; i < accountComboBox.getItemCount(); i++) {
            Account acc = accountComboBox.getItemAt(i);
            if (acc.getId() == currentTransaction.getAccountId()) {
                accountComboBox.setSelectedIndex(i);
                break;
            }
        }

        // Select Category in ComboBox
        // Need to iterate and match by ID as the objects might be different instances
        // if list was reloaded
        for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
            Object item = categoryComboBox.getItemAt(i);
            if (item instanceof Category) {
                Category cat = (Category) item;
                if (cat.getId() == currentTransaction.getCategoryId()) {
                    categoryComboBox.setSelectedItem(cat); // Use setSelectedItem to trigger renderer correctly
                    break;
                }
            }
        }
    }

    private void handleAddNewCategory() {
        // We will create CategoryDialog.java next
        CategoryDialog categoryDialog = new CategoryDialog(parentFrame, categoryController);
        categoryDialog.setVisible(true);

        // After CategoryDialog is closed, repopulate the category ComboBox
        // And try to select the newly added category if available (CategoryDialog would need to return it or allow fetching it)
        Category newCategory = categoryDialog.getNewCategory(); // Assumes CategoryDialog has such a method
        populateCategoryDropdown(); // Repopulate
        if (newCategory != null) {
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                if (categoryComboBox.getItemAt(i) instanceof Category &&
                    ((Category) categoryComboBox.getItemAt(i)).getId() == newCategory.getId()) {
                    categoryComboBox.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            // If no new category was added, or dialog cancelled, reselect a sensible default or previous
            if (categoryComboBox.getItemCount() > 0 && !(categoryComboBox.getItemAt(0) instanceof String)) {
                 categoryComboBox.setSelectedIndex(0); // Select first actual category
            }
        }
    }

    private void saveTransaction() {
        // --- Validation ---
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Description cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            descriptionField.requestFocus();
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be a positive number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                amountField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount format. Please enter a valid number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            amountField.requestFocus();
            return;
        }

        Date selectedDate = (Date) dateSpinner.getValue();
        LocalDate transactionDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        String type = (String) typeComboBox.getSelectedItem();

        Object selectedCategoryObj = categoryComboBox.getSelectedItem();
        if (!(selectedCategoryObj instanceof Category)) {
            JOptionPane.showMessageDialog(this, "Please select a valid category.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            categoryComboBox.requestFocus();
            return;
        }
        Category selectedCategory = (Category) selectedCategoryObj;

        Account selectedAccount = (Account) accountComboBox.getSelectedItem();
        if (selectedAccount == null) { // Should not happen if list is populated
            JOptionPane.showMessageDialog(this, "Please select an account.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            accountComboBox.requestFocus();
            return;
        }

        // --- Create or Update Transaction Object ---
        Transaction transaction = new Transaction(
            0, // ID will be set by DB for new, or use currentTransaction.getId() for edit
            description,
            amount,
            transactionDate,
            type,
            selectedCategory.getId(),
            selectedAccount.getId(),
            selectedCategory.getName(), // For DTO consistency, though DAO might re-fetch
            selectedAccount.getName()   // For DTO consistency
        );


        try {
            if (currentTransaction == null) { // Adding new transaction
                transactionController.addTransaction(transaction);
                JOptionPane.showMessageDialog(this, "Transaction added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else { // Editing existing transaction
                transaction.setId(currentTransaction.getId()); // Set the ID for update
                transactionController.updateTransaction(transaction);
                JOptionPane.showMessageDialog(this, "Transaction updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            dispose(); // Close dialog on successful save
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving transaction: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}