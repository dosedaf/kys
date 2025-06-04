package src.controller;

import java.sql.SQLException;
import java.util.List;
// No need to import ArrayList if only used for return type inference in catch,
// but List is the correct return type.

import src.dao.AccountDAO;
import src.model.Account;

public class AccountController {
    private AccountDAO dao;

    public AccountController() {
        dao = new AccountDAO();
    }

    /**
     * Adds a new account.
     * @param a The account to add.
     * @throws SQLException if a database error occurs.
     */
    public void addAccount(Account a) throws SQLException {
        // No try-catch here; let the calling UI code handle SQLException
        // to display an appropriate error message.
        dao.insert(a);
    }

    /**
     * Retrieves all accounts.
     * @return A list of all accounts.
     * @throws SQLException if a database error occurs.
     */
    public List<Account> getAccounts() throws SQLException {
        // No try-catch here; let the calling UI code handle SQLException.
        return dao.getAll();
    }

    /**
     * Updates an existing account.
     * @param a The account to update.
     * @throws SQLException if a database error occurs.
     */
    public void updateAccount(Account a) throws SQLException {
        dao.update(a);
    }

    /**
     * Deletes an account by its ID.
     * @param id The ID of the account to delete.
     * @throws SQLException if a database error occurs.
     */
    public void deleteAccount(int id) throws SQLException {
        dao.delete(id);
    }

    /**
     * Retrieves an account by its ID.
     * @param id The ID of the account.
     * @return The Account object if found, null otherwise.
     * @throws SQLException if a database error occurs.
     */
    public Account getAccountById(int id) throws SQLException {
        return dao.getById(id); // Assuming AccountDAO has getById(id)
    }
}