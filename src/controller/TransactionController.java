package src.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
// No need for ArrayList import if only for return type inference in catch.

import src.dao.AccountDAO; // Needed for combined operations
import src.dao.DBConnection; // Needed for managing Connection
import src.dao.TransactionDAO;
import src.model.Transaction;

public class TransactionController {
    private TransactionDAO transactionDAO;
    private AccountDAO accountDAO; // Added AccountDAO

    public TransactionController() {
        this.transactionDAO = new TransactionDAO();
        this.accountDAO = new AccountDAO(); // Initialize AccountDAO
    }

    /**
     * Retrieves all transactions with category and account names populated.
     * @return A list of transactions.
     * @throws SQLException if a database error occurs.
     */
    public List<Transaction> getTransactions() throws SQLException {
        return transactionDAO.getAll(); // Corrected to return the list from DAO
                                        // Assumes TransactionDAO.getAll() now joins and populates names
    }

    /**
     * Adds a new transaction and updates the associated account balance.
     * Manages the database connection and transaction.
     * @param t The transaction to add.
     * @throws SQLException if a database error occurs.
     */
    public void addTransaction(Transaction t) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            transactionDAO.insertTransactionAndUpdateAccount(t, this.accountDAO, conn);

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    // Log rollback failure (or add to original exception)
                    System.err.println("Transaction rollback failed: " + ex.getMessage());
                }
            }
            throw e; // Re-throw original exception for UI to handle
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Failed to close connection: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Updates an existing transaction and adjusts account balances accordingly.
     * Manages the database connection and transaction.
     * @param updatedTransaction The transaction with updated details.
     * @throws SQLException if a database error occurs or the original transaction isn't found.
     */
    public void updateTransaction(Transaction updatedTransaction) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Fetch the original transaction to correctly adjust balances
            // Assumes TransactionDAO.getById(int, Connection) is available
            Transaction oldTransaction = transactionDAO.getById(updatedTransaction.getId(), conn);
            if (oldTransaction == null) {
                throw new SQLException("Original transaction not found for update. ID: " + updatedTransaction.getId());
            }

            transactionDAO.updateTransactionAndUpdateAccounts(oldTransaction, updatedTransaction, this.accountDAO, conn);

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Transaction rollback failed: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Failed to close connection: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Deletes a transaction by its ID and reverts the associated account balance.
     * Manages the database connection and transaction.
     * @param transactionId The ID of the transaction to delete.
     * @throws SQLException if a database error occurs.
     */
    public void deleteTransaction(int transactionId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            transactionDAO.deleteTransactionAndUpdateAccount(transactionId, this.accountDAO, conn);

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Transaction rollback failed: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Failed to close connection: " + ex.getMessage());
                }
            }
        }
    }
}