package src.controller;

import java.sql.SQLException;

import src.dao.AccountDAO;
import src.model.Account;

import java.util.ArrayList;
import java.util.List;

public class AccountController {
    private AccountDAO dao;
    
    public AccountController() {
        dao = new AccountDAO();
    }
    
    public void addAccount(Account a) {
        try {
            dao.insert(a);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Account> getAccounts() {
        try {
            return dao.getAll();
        } catch(SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void updateAccount(Account a) {
        try {
            dao.update(a);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAccount(int id) {
        try {
            dao.delete(id);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
}
