package src.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import src.model.Account;

public class AccountTableModel extends AbstractTableModel {

    private List<Account> accounts;
    private final String[] columnNames = {"ID", "Name", "Balance"};

    public AccountTableModel() {
        this.accounts = new ArrayList<>();
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = new ArrayList<>(accounts); // Use a copy
        fireTableDataChanged();
    }

    public Account getAccountAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < accounts.size()) {
            return accounts.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return accounts.size();
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
        Account account = accounts.get(rowIndex);
        switch (columnIndex) {
            case 0: // ID
                return account.getId();
            case 1: // Name
                return account.getName();
            case 2: // Balance
                return account.getBalance();
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // ID
                return Integer.class;
            case 1: // Name
                return String.class;
            case 2: // Balance
                return BigDecimal.class;
            default:
                return Object.class;
        }
    }
}