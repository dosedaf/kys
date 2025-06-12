package src.view;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import src.controller.AccountController;
import src.controller.CategoryController;
import src.controller.TransactionController;

public class MainFrame extends JFrame {
    private DashboardPanel dashboardPanel;
    private AccountController accountController;
    private CategoryController categoryController;
    private TransactionController transactionController;

    public MainFrame() {
        this.accountController = new AccountController();
        this.categoryController = new CategoryController();
        this.transactionController = new TransactionController();

        setTitle("KYS Financial Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Start with a reasonable compact size, can be resized
        setSize(900, 700);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        dashboardPanel = new DashboardPanel(transactionController, accountController, categoryController, this);
        add(dashboardPanel, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu manageMenu = new JMenu("Manage");
        JMenuItem manageCategoriesItem = new JMenuItem("Categories...");
        manageCategoriesItem.addActionListener(e -> openCategoryManagement());
        manageMenu.add(manageCategoriesItem);

        JMenuItem manageAccountsItem = new JMenuItem("Accounts...");
        manageAccountsItem.addActionListener(e -> openAccountManagement());
        manageMenu.add(manageAccountsItem);

        menuBar.add(manageMenu);
        setJMenuBar(menuBar);
    }

    private void openCategoryManagement() {
        CategoryManagementDialog categoryDialog = new CategoryManagementDialog(this, categoryController,
                transactionController);
        categoryDialog.setVisible(true);
        dashboardPanel.refreshUIData();
    }

    private void openAccountManagement() {
        AccountManagementDialog accountDialog = new AccountManagementDialog(this, accountController,
                transactionController);
        accountDialog.setVisible(true);
        dashboardPanel.refreshUIData();
    }
}