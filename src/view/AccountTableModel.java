package src.view;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import src.model.Account;

public class AccountTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Name", "Balance"};
    private List<Account> accounts;
    private final DecimalFormat currencyFormatter = new DecimalFormat("###,##0.00");

    public AccountTableModel() {
        this.accounts = new ArrayList<>();
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
        fireTableDataChanged();
    }

    public Account getAccountAt(int rowIndex) {
        return accounts.get(rowIndex);
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
            case 0:
                return account.getId();
            case 1:
                return account.getName();
            case 2:
                return currencyFormatter.format(account.getBalance());
            default:
                return null;
        }
    }
}