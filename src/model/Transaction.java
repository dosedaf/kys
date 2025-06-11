package src.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private int id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private String type;
    private int categoryId;
    private int accountId;
    private String categoryName;
    private String accountName;

    
    public Transaction(String description, BigDecimal amount, LocalDate date, String type,
                       int categoryId, int accountId) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.categoryId = categoryId;
        this.accountId = accountId;
    }

    public Transaction(int id, String description, BigDecimal amount, LocalDate date, String type,
                       int categoryId, int accountId, String categoryName, String accountName) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.categoryName = categoryName; 
        this.accountName = accountName;  
    }

    public Transaction(int id, String description, BigDecimal amount, LocalDate date, String type, int categoryId, int accountId) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.categoryId = categoryId;
        this.accountId = accountId;
    }

    public int getId() {
        return this.id;
    }

    public String getDescription() {
        return this.description;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public String getType() {
        return this.type;
    }

    public int getCategoryId() {
        return this.categoryId;
    }

    public int getAccountId() {
        return this.accountId;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public String getAccountName() {
        return this.accountName;
    }
    public void setId(int id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}