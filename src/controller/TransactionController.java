package src.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import src.dao.AccountDAO; 
import src.dao.DBConnection;
import src.dao.TransactionDAO;
import src.model.Transaction;

public class TransactionController {
    private TransactionDAO transactionDAO;
    private AccountDAO accountDAO;

    public TransactionController() {
        this.transactionDAO = new TransactionDAO();
        this.accountDAO = new AccountDAO(); 
    }

    public List<Transaction> getTransactions() throws SQLException {
        return transactionDAO.getAll(); 
    }

    public void addTransaction(Transaction t) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            transactionDAO.insertTransactionAndUpdateAccount(t, this.accountDAO, conn);

            conn.commit();
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

    public void updateTransaction(Transaction updatedTransaction) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            Transaction oldTransaction = transactionDAO.getById(updatedTransaction.getId(), conn);
            if (oldTransaction == null) {
                throw new SQLException("Original transaction not found for update. ID: " + updatedTransaction.getId());
            }

            transactionDAO.updateTransactionAndUpdateAccounts(oldTransaction, updatedTransaction, this.accountDAO, conn);

            conn.commit(); 
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

    public void deleteTransaction(int transactionId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            transactionDAO.deleteTransactionAndUpdateAccount(transactionId, this.accountDAO, conn);

            conn.commit(); 
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