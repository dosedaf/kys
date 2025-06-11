package src.controller;

import java.sql.SQLException;
import java.util.List;

import src.dao.AccountDAO;
import src.model.Account;

public class AccountController {
    private AccountDAO dao;

    public AccountController() {
        dao = new AccountDAO();
    }

    public void addAccount(Account a) throws SQLException {
        dao.insert(a);
    }

    public List<Account> getAccounts() throws SQLException {
        return dao.getAll();
    }

    public void updateAccount(Account a) throws SQLException {
        dao.update(a);
    }

    public void deleteAccount(int id) throws SQLException {
        dao.delete(id);
    }

    public Account getAccountById(int id) throws SQLException {
        return dao.getById(id);    }
}