package src.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import src.controller.AccountController;
import src.controller.TransactionController;
import src.model.Account;

public class AccountManagementDialog extends JDialog {
    private AccountController accountController;
    private TransactionController transactionController;
    private JTable accountTable;
    private AccountTableModel accountTableModel;

    public AccountManagementDialog(Frame owner, AccountController accCtrl, TransactionController transCtrl) {
        super(owner, "Manage Accounts", true);
        this.accountController = accCtrl;
        this.transactionController = transCtrl;
        initComponents();
        loadAccounts();
        setSize(500, 400);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        accountTableModel = new AccountTableModel();
        accountTable = new JTable(accountTableModel);
        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(accountTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");
        JButton closeButton = new JButton("Close");

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addAccount());
        deleteButton.addActionListener(e -> deleteAccount());
        closeButton.addActionListener(e -> setVisible(false));
    }

    private void loadAccounts() {
        try {
            List<Account> accounts = accountController.getAccounts();
            accountTableModel.setAccounts(accounts);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading accounts: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addAccount() {
        JTextField nameField = new JTextField();
        JTextField balanceField = new JTextField("0.00");
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Account Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Initial Balance:"));
        panel.add(balanceField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Account name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                BigDecimal balance = new BigDecimal(balanceField.getText().trim().replace(",", "."));
                accountController.addAccount(new Account(name, balance));
                loadAccounts();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid balance format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error adding account: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteAccount() {
        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an account to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Account selectedAccount = accountTableModel.getAccountAt(selectedRow);

        try {
            if (transactionController.hasTransactionsForAccount(selectedAccount.getId())) {
                JOptionPane.showMessageDialog(this,
                    "This account cannot be deleted because it has transactions linked to it.\n" +
                    "Please reassign or delete the associated transactions first.",
                    "Deletion Prevented",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirmation = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete account '" + selectedAccount.getName() + "'?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirmation == JOptionPane.YES_OPTION) {
                accountController.deleteAccount(selectedAccount.getId());
                loadAccounts();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}