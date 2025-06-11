package src.view;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import src.model.Transaction;

import java.text.DecimalFormat;

public class TransactionTableModel extends AbstractTableModel {
    private final DecimalFormat currencyFormatter = new DecimalFormat("###,##0.00");
    private final String[] columnNames = {
            "ID", "Date", "Description", "Amount", "Type", "Category", "Account"
    };
    private List<Transaction> transactions;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TransactionTableModel() {
        this.transactions = new ArrayList<>();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = new ArrayList<>(transactions);
        fireTableDataChanged(); 
    }

    public Transaction getTransactionAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < transactions.size()) {
            return transactions.get(rowIndex);
        }
        return null;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions); 
    }

    @Override
    public int getRowCount() {
        return transactions.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Integer.class; // ID
            case 1: return String.class;  // Date (formatted)
            case 2: return String.class;  // Description
            case 3: return String.class; // Amount
            case 4: return String.class;  // Type
            case 5: return String.class;  // Category Name
            case 6: return String.class;  // Account Name
            default: return Object.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Transaction transaction = transactions.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return transaction.getId();
            case 1:
                return transaction.getDate() != null ? transaction.getDate().format(dateFormatter) : null;
            case 2:
                return transaction.getDescription();
            case 3:
                return currencyFormatter.format(transaction.getAmount());
            case 4:
                return transaction.getType();
            case 5:
                return transaction.getCategoryName() != null ? transaction.getCategoryName() : "N/A"; // from join 
            case 6:
                return transaction.getAccountName() != null ? transaction.getAccountName() : "N/A"; // from join
            default:
                return null;
        }
    }
}