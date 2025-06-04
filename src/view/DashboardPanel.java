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
import src.model.Transaction; // Assuming TransactionTableModel is created

public class DashboardPanel extends JPanel {
    private TransactionController transactionController;
    private AccountController accountController;
    private CategoryController categoryController;
    private JFrame ownerFrame;

    // Transaction components
    private JTable transactionTable;
    private TransactionTableModel transactionTableModel; // You'll need to create/reuse this
    private JButton addTransactionButton, editTransactionButton, deleteTransactionButton;

    // Account components
    private JPanel accountsOverviewPanel; // To display list of accounts and balances
    private JTextArea accountSummaryArea; // Simple display for now

    // Overall Summary components
    private JPanel overallSummaryPanel;
    private JLabel totalBalanceLabel;
    private JLabel totalIncomeLabel; // For displayed transactions
    private JLabel totalExpensesLabel; // For displayed transactions

    public DashboardPanel(TransactionController tCtrl, AccountController aCtrl, CategoryController cCtrl, JFrame owner) {
        this.transactionController = tCtrl;
        this.accountController = aCtrl;
        this.categoryController = cCtrl;
        this.ownerFrame = owner;

        setLayout(new BorderLayout(10, 10)); // Gap between components
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding for the panel

        initComponents();
        loadInitialData();
    }

    private void initComponents() {
        // --- Transactions Panel (CENTER) ---
        JPanel transactionsSection = new JPanel(new BorderLayout(5,5));
        transactionsSection.setBorder(BorderFactory.createTitledBorder("Transactions"));

        transactionTableModel = new TransactionTableModel(); // Needs to be defined (similar to CategoryTableModel)
        transactionTable = new JTable(transactionTableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setFillsViewportHeight(true); // Makes table use entire height of scroll pane

        // Make columns compact if possible
        // transactionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Then set preferred widths manually
        // TableColumnAdjuster tca = new TableColumnAdjuster(transactionTable); // Utility for column widths
        // tca.adjustColumns();


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

        // --- Right Panel (EAST) for Accounts and Overall Summary ---
        JPanel rightPanel = new JPanel();
        // Use BoxLayout for vertical arrangement or GridBagLayout for more control
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(250, 0)); // Preferred width, height adjusts

        // --- Accounts Overview Panel ---
        accountsOverviewPanel = new JPanel(new BorderLayout(5,5));
        accountsOverviewPanel.setBorder(BorderFactory.createTitledBorder("Accounts"));
        accountSummaryArea = new JTextArea(5, 20); // Rows, Columns
        accountSummaryArea.setEditable(false);
        accountSummaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); // Good for tabular data
        JScrollPane accountScrollPane = new JScrollPane(accountSummaryArea);
        accountsOverviewPanel.add(accountScrollPane, BorderLayout.CENTER);
        // Add buttons for "Add Account", "Edit Account" here or use menu bar
        rightPanel.add(accountsOverviewPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0,10))); // Spacer

        // --- Overall Summary Panel ---
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

        // Add sections to main DashboardPanel
        add(transactionsSection, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private void loadInitialData() {
        refreshTransactionTable();
        refreshAccountSummary();
        calculateOverallSummary();
    }
    
    public void refreshUIData() {
        // Call this method after any CRUD operation that might affect displayed data
        refreshTransactionTable();
        refreshAccountSummary();
        calculateOverallSummary();
    }

    private void refreshTransactionTable() {
        try {
            List<Transaction> transactions = transactionController.getTransactions();
            transactionTableModel.setTransactions(transactions); // Assuming this method exists
            calculateTransactionSummary(transactions); // Calculate summary based on loaded transactions
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
                sb.append(String.format("%-15.15s: %10.2f\n", acc.getName(), acc.getBalance()));
                totalSystemBalance = totalSystemBalance.add(acc.getBalance());
            }
            accountSummaryArea.setText(sb.toString());
            totalBalanceLabel.setText(String.format("%.2f", totalSystemBalance));
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
        totalIncomeLabel.setText(String.format("%.2f", income));
        totalExpensesLabel.setText(String.format("%.2f", expenses));
    }
    
    private void calculateOverallSummary() {
        // This is called after both transactions and accounts are refreshed.
        // totalBalanceLabel is updated in refreshAccountSummary.
        // totalIncomeLabel and totalExpensesLabel are updated based on the current view of transactions.
        // If you need a more global summary (e.g., total income/expenses across all time not just visible),
        // you might need dedicated controller/DAO methods.
        List<Transaction> currentTransactions = transactionTableModel.getTransactions(); // Get current list from model
        calculateTransactionSummary(currentTransactions);
    }


    private void addTransaction() {
        TransactionDialog dialog = new TransactionDialog(ownerFrame, transactionController, accountController, categoryController, null, this::refreshUIData);
        dialog.setVisible(true);
    }

    private void editTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Ensure your TransactionTableModel has a method like getTransactionAt(row)
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
                    refreshUIData(); // Refresh table and summaries
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(ownerFrame, "Error deleting transaction: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(ownerFrame, "Please select a transaction to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        }
    }
}