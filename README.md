# KYS Financial Tracker

KYS (Keep Your Savings) is a clean, modern, and functional desktop application designed for personal financial tracking. Built with Java Swing, it provides a straightforward way to manage your income and expenses across different accounts and categories. The application connects to a MariaDB database to ensure data persistence and integrity.

![Java](https://img.shields.io/badge/Java-11%2B-blue?logo=java&logoColor=white)
![Swing](https://img.shields.io/badge/UI-Java%20Swing-orange)
![Database](https://img.shields.io/badge/Database-MariaDB-blue?logo=mariadb&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green)

---

## Core Features

- **Modern User Interface:** A clean, single-panel dashboard built with the modern FlatLaf Look and Feel.
- **Transaction Management:** Full CRUD (Create, Read, Update, Delete) functionality for all your income and expense records.
- **Account & Category Management:** Easily add, delete, and manage your financial accounts (e.g., Bank, Cash, E-Wallet) and spending categories through dedicated management dialogs.
- **On-the-Go Category Creation:** Add new spending or income categories directly from the "Add Transaction" screen without interrupting your workflow.
- **Data Integrity Protection:** The application intelligently prevents the deletion of accounts or categories that are currently linked to existing transactions, protecting your financial history.
- **Dashboard Summary:** The main view provides an at-a-glance summary of your account balances, as well as total income and expenses for the displayed period.
- **Localized Number Formatting:** All currency values are displayed in a readable format (`25.000,00`), making large numbers easy to comprehend.

## Technology Stack

- **Language:** Java (JDK 11 or newer recommended)
- **UI Toolkit:** Java Swing
- **Look and Feel:** [FlatLaf](https://www.formdev.com/flatlaf/) (for the modern UI)
- **Database:** MariaDB (also compatible with MySQL)
- **Database Connectivity:** JDBC (Java Database Connectivity)
- **Architecture:** Model-View-Controller (MVC)

---

## Setup and Installation Guide

Follow these steps to get the KYS application running on your local machine.

### 1. Prerequisites

Make sure you have the following software installed:
- **Java Development Kit (JDK):** Version 11 or higher.
- **MariaDB Server:** Or a compatible MySQL server.
- **Git:** For cloning the repository.
- **An IDE:** IntelliJ IDEA, Eclipse, or VS Code with Java extensions are recommended.

### 2. Database Setup

The application requires a database named `kys` with three specific tables.

1.  **Create the Database:** Run the following SQL command in your database client:
    ```sql
    CREATE DATABASE IF NOT EXISTS kys;
    ```
2.  **Select the Database:**
    ```sql
    USE kys;
    ```
3.  **Create the Tables:** Run the following SQL script to create the `accounts`, `categories`, and `transactions` tables with the correct structure and relationships.
    ```sql
    CREATE TABLE accounts (
        id INT(11) NOT NULL AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL,
        balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
        PRIMARY KEY (id)
    );

    CREATE TABLE categories (
        id INT(11) NOT NULL AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL,
        description VARCHAR(255) DEFAULT NULL,
        type ENUM('INCOME_CATEGORY','EXPENSE_CATEGORY') NOT NULL,
        PRIMARY KEY (id)
    );

    CREATE TABLE transactions (
        id INT(11) NOT NULL AUTO_INCREMENT,
        description VARCHAR(255) NOT NULL,
        amount DECIMAL(15,2) NOT NULL,
        date DATE NOT NULL,
        type ENUM('INCOME','EXPENSE') NOT NULL,
        category_id INT(11) NOT NULL,
        account_id INT(11) NOT NULL,
        PRIMARY KEY (id),
        KEY `fk_transactions_category` (`category_id`),
        KEY `fk_transactions_account` (`account_id`),
        CONSTRAINT `fk_transactions_account` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
        CONSTRAINT `fk_transactions_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
    );
    ```
    _**Note:** If you have old tables, it's best to drop them and recreate them with this script to ensure they are correct. If they have data, you can clear them using `TRUNCATE TABLE accounts;`, `TRUNCATE TABLE categories;`, etc._

### 3. Application Configuration

1.  **Clone the Repository:**
    ```sh
    git clone [your-repository-url]
    cd [repository-folder]
    ```
2.  **Add Dependencies:** You need to add two `.jar` files to your project's classpath.
    - **MariaDB JDBC Driver:** Download from the [official MariaDB website](https://mariadb.com/downloads/connectors/connectors-data-access/java-connector/).
    - **FlatLaf JAR:** Download from the [FlatLaf GitHub repository](https://github.com/JFormDesigner/flatlaf/releases).
    - In your IDE (like IntelliJ or Eclipse), go to your project settings/properties, find the "Libraries" or "Build Path" section, and add these two JAR files as external dependencies.

3.  **Configure Database Connection:** Open the file `src/dao/DBConnection.java` and update the connection details to match your local database setup.
    ```java
    package src.dao;

    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.SQLException;

    public class DBConnection {
        private static final String URL = "jdbc:mysql://localhost:3306/kys";
        private static final String USERNAME = "root"; // Your DB username
        private static final String PASSWORD = "";     // Your DB password

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
    }
    ```

### 4. Running the Application

Once the database is set up and dependencies are added, you can run the application by executing the `main` method in the `src/Main.java` file from your IDE.

---

## How to Use the Application

1.  **First-Time Use:** When you first run the application, the tables will be empty. Before you can add any transactions, you must create at least one account and one category.
    - Go to the **Manage -> Accounts...** menu item to add a new financial account (e.g., "BCA Bank", "Gopay E-Wallet").
    - Go to the **Manage -> Categories...** menu item to add a new category (e.g., "Food", "Salary", "Transportation").

2.  **Adding a Transaction:**
    - On the main dashboard, click the **"Add"** button.
    - Fill in the transaction details in the dialog that appears. You can even add a new category on the fly by clicking the **"+"** button next to the category dropdown.
    - Click **"Save"**. The new transaction will appear in the main table, and your account balances and summaries will update automatically.

3.  **Editing or Deleting:**
    - Select a transaction in the table.
    - Click the **"Edit"** or **"Delete"** button to modify or remove the record. The application will ask for confirmation before deleting.

## License

This project is licensed under the MIT License.