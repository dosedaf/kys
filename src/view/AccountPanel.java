package src.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.math.BigDecimal; // For TableRowSorter comparator
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator; // For sorting BigDecimal
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

import src.controller.AccountController;
import src.model.Account;

public class AccountPanel extends JPanel {

    private AccountController accountController;
    private JTable accountTable;
    private AccountTableModel accountTableModel;
    private JButton addButton, editButton, deleteButton;

    public AccountPanel(AccountController accountController) {
        this.accountController = accountController;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        initComponents();
        loadAccounts();
    }

    private void initComponents() {
        // Account Table
        accountTableModel = new AccountTableModel();
        accountTable = new JTable(accountTableModel);
        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountTable.setFillsViewportHeight(true);
        accountTable.setBackground(Color.WHITE);
        accountTable.getTableHeader().setBackground(Color.LIGHT_GRAY);
        accountTable.getTableHeader().setForeground(Color.BLACK);

        // Setup sorter for the table
        TableRowSorter<AccountTableModel> sorter = new TableRowSorter<>(accountTableModel);
        sorter.setComparator(2, Comparator.comparing(v -> (BigDecimal) v, BigDecimal::compareTo)); // Column 2 is 'Balance'
        accountTable.setRowSorter(sorter);


        JScrollPane scrollPane = new JScrollPane(accountTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        addButton = new JButton("Add New Account");
        styleButton(addButton);
        buttonPanel.add(addButton);

        editButton = new JButton("Edit Selected Account");
        styleButton(editButton);
        buttonPanel.add(editButton);

        deleteButton = new JButton("Delete Selected Account");
        styleButton(deleteButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        addButton.addActionListener(e -> openAccountDialog(null)); // null for new account
        editButton.addActionListener(e -> {
            int selectedRow = accountTable.getSelectedRow();
            if (selectedRow >= 0) {
                int modelRow = accountTable.convertRowIndexToModel(selectedRow);
                Account accountToEdit = accountTableModel.getAccountAt(modelRow);
                if (accountToEdit != null) {
                    openAccountDialog(accountToEdit);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an account to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> deleteSelectedAccount());
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(220, 220, 220));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
    }

    public void loadAccounts() {
        try {
            List<Account> accounts = accountController.getAccounts();
            accountTableModel.setAccounts(accounts);
        } catch (SQLException e) {
            accountTableModel.setAccounts(new ArrayList<>()); // Clear table on error
            JOptionPane.showMessageDialog(this, "Error loading accounts: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openAccountDialog(Account account) {
        AccountDialog dialog = new AccountDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                accountController,
                account);
        dialog.setVisible(true);
        loadAccounts(); // Refresh list after dialog closes
    }

    private void deleteSelectedAccount() {
        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = accountTable.convertRowIndexToModel(selectedRow);
            Account accountToDelete = accountTableModel.getAccountAt(modelRow);
            if (accountToDelete != null) {
                // Check for dependencies (e.g., transactions associated with this account)
                // This is a simplified check. A real app might need more robust logic
                // or rely on database foreign key constraints to prevent deletion.
                // For now, we'll just ask for confirmation.
                int confirmation = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete account: '" + accountToDelete.getName() + "'?\n" +
                        "This might fail if transactions are linked to this account.",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirmation == JOptionPane.YES_OPTION) {
                    try {
                        accountController.deleteAccount(accountToDelete.getId());
                        JOptionPane.showMessageDialog(this, "Account deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadAccounts(); // Refresh list
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(this, "Error deleting account: " + e.getMessage() +
                                "\n(It might be in use by existing transactions).", "Database Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an account to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
}