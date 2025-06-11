package src.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import src.controller.AccountController;
import src.controller.CategoryController;
import src.controller.TransactionController;
import src.model.Account;
import src.model.Transaction; 

import java.text.DecimalFormat;

public class DashboardPanel extends JPanel {
    private TransactionController transactionController;
    private AccountController accountController;
    private CategoryController categoryController;
    private JFrame ownerFrame;

    // Transaction components
    private JTable transactionTable;
    private TransactionTableModel transactionTableModel;
    private JButton addTransactionButton, editTransactionButton, deleteTransactionButton;

    // Account components
    private JPanel accountsOverviewPanel; 
    private JTextArea accountSummaryArea;

    // Overall Summary components
    private JPanel overallSummaryPanel;
    private JLabel totalBalanceLabel;
    private JLabel totalIncomeLabel;
    private JLabel totalExpensesLabel; 

    private final DecimalFormat currencyFormatter = new DecimalFormat("Rp ###,##0.00");

    public DashboardPanel(TransactionController tCtrl, AccountController aCtrl, CategoryController cCtrl, JFrame owner) {
        this.transactionController = tCtrl;
        this.accountController = aCtrl;
        this.categoryController = cCtrl;
        this.ownerFrame = owner;

        setLayout(new BorderLayout(10, 10)); 
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 

        initComponents();
        loadInitialData();
    }

    private void initComponents() {
        JPanel transactionsSection = new JPanel(new BorderLayout(5,5));
        transactionsSection.setBorder(BorderFactory.createTitledBorder("Transactions"));

        transactionTableModel = new TransactionTableModel();
        transactionTable = new JTable(transactionTableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setFillsViewportHeight(true); 

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        transactionsSection.add(scrollPane, BorderLayout.CENTER);

        JPanel transactionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addTransactionButton = new JButton("Add");
        editTransactionButton = new JButton("Edit");
        deleteTransactionButton = new JButton("Delete");
        transactionButtonsPanel.add(addTransactionButton);
        transactionButtonsPanel.add(editTransactionButton);
        transactionButtonsPanel.add(deleteTransactionButton);
        transactionsSection.add(transactionButtonsPanel, BorderLayout.SOUTH);

        addTransactionButton.addActionListener(e -> addTransaction());
        editTransactionButton.addActionListener(e -> editTransaction());
        deleteTransactionButton.addActionListener(e -> deleteTransaction());

        JPanel rightPanel = new JPanel();

        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(250, 0)); 

        accountsOverviewPanel = new JPanel(new BorderLayout(5,5));
        accountsOverviewPanel.setBorder(BorderFactory.createTitledBorder("Accounts"));
        accountSummaryArea = new JTextArea(5, 20); 
        accountSummaryArea.setEditable(false);
        accountSummaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); 
        JScrollPane accountScrollPane = new JScrollPane(accountSummaryArea);
        accountsOverviewPanel.add(accountScrollPane, BorderLayout.CENTER);

        rightPanel.add(accountsOverviewPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0,10))); // Spacer

        overallSummaryPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // Rows, Cols, Hgap, Vgap
        overallSummaryPanel.setBorder(BorderFactory.createTitledBorder("Summary (Visible Transactions)"));
        totalBalanceLabel = new JLabel("N/A"); // Calculated from all account balances
        totalIncomeLabel = new JLabel("0.00");
        totalExpensesLabel = new JLabel("0.00");
        overallSummaryPanel.add(new JLabel("Total Balance:"));
        overallSummaryPanel.add(totalBalanceLabel);
        overallSummaryPanel.add(new JLabel("Total Income:"));
        overallSummaryPanel.add(totalIncomeLabel);
        overallSummaryPanel.add(new JLabel("Total Expenses:"));
        overallSummaryPanel.add(totalExpensesLabel);
        rightPanel.add(overallSummaryPanel);

        add(transactionsSection, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private void loadInitialData() {
        refreshTransactionTable();
        refreshAccountSummary();
        calculateOverallSummary();
    }
    
    public void refreshUIData() {
        // call di setiap crud operation yg affect data
        refreshTransactionTable();
        refreshAccountSummary();
        calculateOverallSummary();
    }

    private void refreshTransactionTable() {
        try {
            List<Transaction> transactions = transactionController.getTransactions();
            transactionTableModel.setTransactions(transactions);
            calculateTransactionSummary(transactions); 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(ownerFrame, "Error loading transactions: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAccountSummary() {
        try {
            List<Account> accounts = accountController.getAccounts();
            StringBuilder sb = new StringBuilder();
            BigDecimal totalSystemBalance = BigDecimal.ZERO;
            for (Account acc : accounts) {
                sb.append(String.format("%-15.15s: %s\n", acc.getName(), currencyFormatter.format(acc.getBalance())));
                totalSystemBalance = totalSystemBalance.add(acc.getBalance());
            }
            accountSummaryArea.setText(sb.toString());
            totalBalanceLabel.setText(currencyFormatter.format(totalSystemBalance));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(ownerFrame, "Error loading accounts: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            accountSummaryArea.setText("Error loading accounts.");
            totalBalanceLabel.setText("N/A");
        }
    }

    private void calculateTransactionSummary(List<Transaction> transactions) {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;
        if (transactions != null) {
            for (Transaction t : transactions) {
                if ("INCOME".equalsIgnoreCase(t.getType())) {
                    income = income.add(t.getAmount());
                } else if ("EXPENSE".equalsIgnoreCase(t.getType())) {
                    expenses = expenses.add(t.getAmount());
                }
            }
        }
        totalIncomeLabel.setText(currencyFormatter.format(income));
        totalExpensesLabel.setText(currencyFormatter.format(expenses));
    }
    
    private void calculateOverallSummary() {
        // called after transactions n accounts r refreshed
        // totalBalanceLabel is updated in refreshAccountSummary.
        // totalIncomeLabel n totalExpensesLabel r updated based on the current view of transactions.
        List<Transaction> currentTransactions = transactionTableModel.getTransactions(); 
        calculateTransactionSummary(currentTransactions);
    }


    private void addTransaction() {
    try {
        boolean hasAccounts = !accountController.getAccounts().isEmpty();
        boolean hasCategories = !categoryController.getCategories().isEmpty();

        if (!hasAccounts || !hasCategories) {
            String message = "You must create at least one account and one category before adding a transaction.";
            if (!hasAccounts && !hasCategories) {
                message = "You must create at least one account and one category before adding a transaction.\nPlease use the 'Manage' menu to add them.";
            } else if (!hasAccounts) {
                message = "You must create at least one account before adding a transaction.\nPlease use the 'Manage -> Accounts' menu item.";
            } else { // !hasCategories
                message = "You must create at least one category before adding a transaction.\nPlease use the 'Manage -> Categories' menu item.";
            }
            JOptionPane.showMessageDialog(ownerFrame, message, "Prerequisites Missing", JOptionPane.WARNING_MESSAGE);
            return; // stop n dont open dialog 
        }

        // checks passed, open dialog
        TransactionDialog dialog = new TransactionDialog(ownerFrame, transactionController, accountController, categoryController, null, this::refreshUIData);
        dialog.setVisible(true);

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(ownerFrame, "Error checking for accounts/categories: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void editTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow >= 0) {
            Transaction selectedTransaction = transactionTableModel.getTransactionAt(selectedRow);
            TransactionDialog dialog = new TransactionDialog(ownerFrame, transactionController, accountController, categoryController, selectedTransaction, this::refreshUIData);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(ownerFrame, "Please select a transaction to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow >= 0) {
            Transaction selectedTransaction = transactionTableModel.getTransactionAt(selectedRow);
            int confirmation = JOptionPane.showConfirmDialog(ownerFrame,
                    "Are you sure you want to delete transaction: '" + selectedTransaction.getDescription() + "'?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirmation == JOptionPane.YES_OPTION) {
                try {
                    transactionController.deleteTransaction(selectedTransaction.getId());
                    JOptionPane.showMessageDialog(ownerFrame, "Transaction deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshUIData(); 
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(ownerFrame, "Error deleting transaction: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(ownerFrame, "Please select a transaction to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        }
    }
}