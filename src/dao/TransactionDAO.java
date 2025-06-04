package src.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date; // For converting LocalDate to sql.Date
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // For Statement.RETURN_GENERATED_KEYS
import java.util.ArrayList;
import java.util.List;

import src.model.Transaction; // Your Transaction model
// Assuming AccountDAO is in the same package or imported correctly
// import src.dao.AccountDAO; (already in same package)

public class TransactionDAO {

    // --- Original Methods (potentially for standalone use or later refactoring) ---
    public void insert(Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions (description, amount, date, type, category_id, account_id) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, t.getDescription());
            stmt.setBigDecimal(2, t.getAmount());
            stmt.setDate(3, Date.valueOf(t.getDate()));
            stmt.setString(4, t.getType());
            stmt.setInt(5, t.getCategoryId());
            stmt.setInt(6, t.getAccountId());
            stmt.executeUpdate();
            // If your Transaction model has an 'id' field and you want to set it:
            // try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            // if (generatedKeys.next()) {
            // t.setId(generatedKeys.getInt(1));
            // }
            // }
        }
    }

    public List<Transaction> getAll() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        // SQL to join with categories and accounts to get names directly
        String sql = "SELECT t.id, t.description, t.amount, t.date, t.type, " +
                     "t.category_id, c.name as category_name, " +
                     "t.account_id, a.name as account_name " +
                     "FROM transactions t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "LEFT JOIN accounts a ON t.account_id = a.id " +
                     "ORDER BY t.date DESC, t.id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transaction t = new Transaction(
                        rs.getInt("id"),
                        rs.getString("description"),
                        rs.getBigDecimal("amount"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("type"),
                        rs.getInt("category_id"),
                        rs.getInt("account_id"),
                        rs.getString("category_name"), // Now populated
                        rs.getString("account_name")   // Now populated
                );
                list.add(t);
            }
        }
        return list;
    }

    // Original getById - uses its own connection
    public Transaction getById(int id) throws SQLException {
        // This query should also join to get names if the Transaction object expects them
        String sql = "SELECT t.id, t.description, t.amount, t.date, t.type, " +
                     "t.category_id, c.name as category_name, " +
                     "t.account_id, a.name as account_name " +
                     "FROM transactions t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "LEFT JOIN accounts a ON t.account_id = a.id " +
                     "WHERE t.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Transaction(
                            rs.getInt("id"),
                            rs.getString("description"),
                            rs.getBigDecimal("amount"),
                            rs.getDate("date").toLocalDate(),
                            rs.getString("type"),
                            rs.getInt("category_id"),
                            rs.getInt("account_id"),
                            rs.getString("category_name"),
                            rs.getString("account_name")
                    );
                }
            }
        }
        return null;
    }


    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void update(Transaction t) throws SQLException {
        String sql = "UPDATE transactions SET description=?, amount=?, date=?, type=?, category_id=?, account_id=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getDescription());
            stmt.setBigDecimal(2, t.getAmount());
            stmt.setDate(3, Date.valueOf(t.getDate()));
            stmt.setString(4, t.getType());
            stmt.setInt(5, t.getCategoryId());
            stmt.setInt(6, t.getAccountId());
            stmt.setInt(7, t.getId());
            stmt.executeUpdate();
        }
    }

    // --- New/Modified Methods for Controller-Managed Transactions ---

    /**
     * Retrieves a transaction by its ID using an existing connection.
     * Includes category and account names.
     * @param id The ID of the transaction.
     * @param conn The existing database connection.
     * @return The Transaction object if found, otherwise null.
     * @throws SQLException
     */
    public Transaction getById(int id, Connection conn) throws SQLException {
         String sql = "SELECT t.id, t.description, t.amount, t.date, t.type, " +
                     "t.category_id, c.name as category_name, " +
                     "t.account_id, a.name as account_name " +
                     "FROM transactions t " +
                     "LEFT JOIN categories c ON t.category_id = c.id " +
                     "LEFT JOIN accounts a ON t.account_id = a.id " +
                     "WHERE t.id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Transaction(
                            rs.getInt("id"),
                            rs.getString("description"),
                            rs.getBigDecimal("amount"),
                            rs.getDate("date").toLocalDate(),
                            rs.getString("type"),
                            rs.getInt("category_id"),
                            rs.getInt("account_id"),
                            rs.getString("category_name"),
                            rs.getString("account_name")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Inserts a new transaction and updates the corresponding account's balance.
     * Uses the provided AccountDAO to perform account updates.
     * THIS METHOD EXPECTS TO BE PART OF A LARGER TRANSACTION (managed externally by the Controller).
     * @param t The transaction to insert. (Its ID might be updated if generated).
     * @param accountDAO Instance of AccountDAO to use for updating account balance.
     * @param conn The existing database connection.
     * @throws SQLException
     */
    public void insertTransactionAndUpdateAccount(Transaction t, AccountDAO accountDAO, Connection conn) throws SQLException {
        String sqlInsert = "INSERT INTO transactions (description, amount, date, type, category_id, account_id) VALUES (?,?,?,?,?,?)";
        // Using Statement.RETURN_GENERATED_KEYS to potentially get the new transaction's ID
        try (PreparedStatement stmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, t.getDescription());
            stmt.setBigDecimal(2, t.getAmount());
            stmt.setDate(3, Date.valueOf(t.getDate()));
            stmt.setString(4, t.getType());
            stmt.setInt(5, t.getCategoryId());
            stmt.setInt(6, t.getAccountId());
            stmt.executeUpdate();

            // If your Transaction model's 'id' field should be updated with the generated ID:
            // try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            // if (generatedKeys.next()) {
            // t.setId(generatedKeys.getInt(1)); // Assuming your Transaction model has setId()
            // } else {
            // throw new SQLException("Creating transaction failed, no ID obtained.");
            // }
            // }
        }

        BigDecimal amountChange = t.getAmount();
        if ("Expense".equalsIgnoreCase(t.getType())) {
            amountChange = amountChange.negate(); // Negative for expense
        }
        accountDAO.adjustBalance(t.getAccountId(), amountChange, conn);
    }

    /**
     * Deletes a transaction and reverts the corresponding account's balance.
     * THIS METHOD EXPECTS TO BE PART OF A LARGER TRANSACTION.
     * @param transactionId The ID of the transaction to delete.
     * @param accountDAO Instance of AccountDAO.
     * @param conn The existing database connection.
     * @throws SQLException
     */
    public void deleteTransactionAndUpdateAccount(int transactionId, AccountDAO accountDAO, Connection conn) throws SQLException {
        Transaction t = this.getById(transactionId, conn); // Uses the connection-aware getById
        if (t == null) {
            throw new SQLException("Transaction with ID " + transactionId + " not found for deletion.");
        }

        String sqlDelete = "DELETE FROM transactions WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlDelete)) {
            stmt.setInt(1, transactionId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting transaction failed, no rows affected. ID: " + transactionId);
            }
        }

        BigDecimal balanceAdjustment;
        if ("Expense".equalsIgnoreCase(t.getType())) {
            balanceAdjustment = t.getAmount(); // Add back the positive expense amount
        } else { // Income
            balanceAdjustment = t.getAmount().negate(); // Subtract the positive income amount
        }
        accountDAO.adjustBalance(t.getAccountId(), balanceAdjustment, conn);
    }

    /**
     * Updates a transaction and appropriately adjusts balances for the old and new accounts.
     * THIS METHOD EXPECTS TO BE PART OF A LARGER TRANSACTION.
     * @param oldTransactionData The transaction data before update (fetched using getById(id, conn)).
     * @param newTransaction The new transaction data (contains the ID of the transaction to update).
     * @param accountDAO Instance of AccountDAO.
     * @param conn The existing database connection.
     * @throws SQLException
     */
    public void updateTransactionAndUpdateAccounts(Transaction oldTransactionData, Transaction newTransaction, AccountDAO accountDAO, Connection conn) throws SQLException {
        // 1. Revert impact of the old transaction details on its account
        BigDecimal oldAmountReversion;
        if ("Expense".equalsIgnoreCase(oldTransactionData.getType())) {
            oldAmountReversion = oldTransactionData.getAmount(); // Add back positive expense amount
        } else { // Income
            oldAmountReversion = oldTransactionData.getAmount().negate(); // Subtract positive income amount
        }
        accountDAO.adjustBalance(oldTransactionData.getAccountId(), oldAmountReversion, conn);

        // 2. Apply impact of the new transaction details on its new/current account
        BigDecimal newAmountImpact = newTransaction.getAmount();
        if ("Expense".equalsIgnoreCase(newTransaction.getType())) {
            newAmountImpact = newAmountImpact.negate(); // Subtract new expense amount
        }
        // For income, newAmountImpact is positive and will be added.
        accountDAO.adjustBalance(newTransaction.getAccountId(), newAmountImpact, conn);

        // 3. Update the transaction details in the database
        String sqlUpdate = "UPDATE transactions SET description=?, amount=?, date=?, type=?, category_id=?, account_id=? WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            stmt.setString(1, newTransaction.getDescription());
            stmt.setBigDecimal(2, newTransaction.getAmount());
            stmt.setDate(3, Date.valueOf(newTransaction.getDate()));
            stmt.setString(4, newTransaction.getType());
            stmt.setInt(5, newTransaction.getCategoryId());
            stmt.setInt(6, newTransaction.getAccountId());
            stmt.setInt(7, newTransaction.getId()); // ID for WHERE clause
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                 throw new SQLException("Updating transaction failed, no rows affected. ID: " + newTransaction.getId());
            }
        }
    }
}