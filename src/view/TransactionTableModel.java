package src.view; // Assuming it's in the view package with other UI components

import java.math.BigDecimal; // For amount formatting if needed directly here
import java.time.LocalDate; // For date formatting if needed directly here
import java.time.format.DateTimeFormatter; // For formatting LocalDate
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import src.model.Transaction; // Your Transaction model

public class TransactionTableModel extends AbstractTableModel {

    private List<Transaction> transactions;
    private final String[] columnNames = {
            "ID",
            "Date",
            "Description",
            "Amount",
            "Type",
            "Category",
            "Account"
    };
    // Optional: A formatter for the date column
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TransactionTableModel() {
        this.transactions = new ArrayList<>();
    }

    public TransactionTableModel(List<Transaction> transactions) {
        this.transactions = new ArrayList<>(transactions); // Create a copy
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = new ArrayList<>(transactions); // Create a copy
        fireTableDataChanged(); // Notify the JTable that the data has changed
    }

    public Transaction getTransactionAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < transactions.size()) {
            return transactions.get(rowIndex);
        }
        return null; // Or throw an exception
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        Transaction transaction = transactions.get(rowIndex);
        switch (columnIndex) {
            case 0: // ID
                return transaction.getId();
            case 1: // Date
                LocalDate date = transaction.getDate();
                return (date != null) ? date.format(dateFormatter) : null;
            case 2: // Description
                return transaction.getDescription();
            case 3: // Amount
                return transaction.getAmount(); // JTable will use default renderer for BigDecimal
            case 4: // Type
                return transaction.getType();
            case 5: // Category Name
                return transaction.getCategoryName(); // Assumes this is populated in your Transaction object
            case 6: // Account Name
                return transaction.getAccountName();   // Assumes this is populated in your Transaction object
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // ID
                return Integer.class;
            case 1: // Date
                return String.class; // Since we are formatting it
            case 2: // Description
                return String.class;
            case 3: // Amount
                return BigDecimal.class;
            case 4: // Type
                return String.class;
            case 5: // Category Name
                return String.class;
            case 6: // Account Name
                return String.class;
            default:
                return Object.class;
        }
    }

    // Optional: if you want cells to be editable (we'll assume not for now)
    // @Override
    // public boolean isCellEditable(int rowIndex, int columnIndex) {
    //     return false;
    // }

    // Optional: if cells were editable, you'd need setValueAt
    // @Override
    // public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    //     // Handle cell editing if implemented
    // }
}