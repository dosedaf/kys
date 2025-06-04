package src.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import src.controller.AccountController;
import src.model.Account;

public class AccountDialog extends JDialog {

    private AccountController accountController;
    private Account currentAccount; // null if adding, existing if editing

    private JTextField nameField;
    private JTextField balanceField; // Only for initial balance when adding
    private JLabel balanceLabel;

    private JButton saveButton;
    private JButton cancelButton;

    public AccountDialog(Frame owner, AccountController accountController, Account accountToEdit) {
        super(owner, true); // Modal
        this.accountController = accountController;
        this.currentAccount = accountToEdit;

        setTitle(currentAccount == null ? "Add New Account" : "Edit Account");
        setLayout(new BorderLayout(10,10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().setBackground(Color.WHITE);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();

        if (currentAccount != null) {
            nameField.setText(currentAccount.getName());
            // Balance field is typically not directly editable here once transactions exist
            // It's shown for new accounts. For existing, it's derived.
            balanceField.setText(currentAccount.getBalance().toPlainString());
            balanceField.setEditable(false); // Balance usually not directly editable for existing accounts
            balanceLabel.setText("Current Balance (derived):");
        } else {
            balanceLabel.setText("Initial Balance:");
            balanceField.setEditable(true);
        }
        pack();
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Account Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        // Balance (label changes based on add/edit)
        gbc.gridx = 0;
        gbc.gridy = 1;
        balanceLabel = new JLabel("Initial Balance:"); // Default text
        formPanel.add(balanceLabel, gbc);
        gbc.gridx = 1;
        balanceField = new JTextField(15);
        formPanel.add(balanceField, gbc);


        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        saveButton = new JButton("Save");
        styleButton(saveButton);
        cancelButton = new JButton("Cancel");
        styleButton(cancelButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        saveButton.addActionListener(e -> saveAccount());
        cancelButton.addActionListener(e -> dispose());
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(220, 220, 220));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
    }

    private void saveAccount() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Account name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return;
        }

        BigDecimal balance = BigDecimal.ZERO;
        if (currentAccount == null || balanceField.isEditable()) { // Only parse if it's for a new account or editable
            try {
                balance = new BigDecimal(balanceField.getText().trim());
                if (currentAccount == null && balance.compareTo(BigDecimal.ZERO) < 0) {
                    // Allowing zero initial balance, but not negative for a new account
                    //JOptionPane.showMessageDialog(this, "Initial balance cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    //balanceField.requestFocus();
                    //return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid balance format. Please enter a valid number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                balanceField.requestFocus();
                return;
            }
        }


        try {
            if (currentAccount == null) { // Adding new account
                Account newAccount = new Account(name, balance); // Uses constructor (name, balance)
                accountController.addAccount(newAccount);
                JOptionPane.showMessageDialog(this, "Account added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else { // Editing existing account
                // Typically, only the name is editable. Balance is derived from transactions.
                // If you allow balance editing, ensure it's handled carefully.
                // For now, our DAO update takes name and balance.
                currentAccount.setName(name);
                // If balance field was editable and changed for an existing account:
                // currentAccount.setBalance(balance); // But this is generally not how it's done.
                accountController.updateAccount(currentAccount);
                JOptionPane.showMessageDialog(this, "Account updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            dispose(); // Close dialog
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving account: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}