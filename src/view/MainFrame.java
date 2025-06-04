package src.view; // Assuming a 'view' subpackage for UI classes

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import src.controller.AccountController;
import src.controller.CategoryController;
// Import controllers that will be passed to the panels
import src.controller.TransactionController;

public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private TransactionController transactionController;
    private AccountController accountController;
    private CategoryController categoryController;

    // Panels for each tab
    private TransactionPanel transactionPanel;
    private AccountPanel accountPanel;
    private SummaryPanel summaryPanel;

    public MainFrame(TransactionController transactionController,
                     AccountController accountController,
                     CategoryController categoryController) {
        this.transactionController = transactionController;
        this.accountController = accountController;
        this.categoryController = categoryController;

        setTitle("KYS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 750); // Adjusted size slightly for potentially more content
        setMinimumSize(new Dimension(800, 600)); // Set a minimum size
        setLocationRelativeTo(null); // Center the window

        initComponents();
    }

    private void initComponents() {
        // Set a base background for the content pane, tabs will cover most of it
        // This helps if there are any small gaps or if tabs are transparent (though not by default)
        getContentPane().setBackground(new Color(240, 240, 240)); // A very light gray

        tabbedPane = new JTabbedPane();
        // Potentially style the tabbed pane itself for a monochrome look
        // UIManager.put("TabbedPane.selected", Color.WHITE);
        // UIManager.put("TabbedPane.contentAreaColor", Color.WHITE);
        // UIManager.put("TabbedPane.background", new Color(230,230,230)); // Slightly off-white for tab area

        // --- Transaction Panel ---
        transactionPanel = new TransactionPanel(this.transactionController, this.accountController, this.categoryController);
        // transactionPanel.setBackground(Color.WHITE); // Panel itself sets its background
        tabbedPane.addTab("Transactions", null, transactionPanel, "Manage your income and expenses");


        // --- Account Panel ---
        accountPanel = new AccountPanel(this.accountController);
        // accountPanel.setBackground(Color.WHITE); // Panel itself sets its background
        tabbedPane.addTab("Accounts", null, accountPanel, "Manage your financial accounts");


        // --- Summary Panel ---
        summaryPanel = new SummaryPanel(/* pass relevant controllers if SummaryPanel needs them now */);
        // summaryPanel.setBackground(Color.WHITE); // Panel itself sets its background
        tabbedPane.addTab("Summary", null, summaryPanel, "View financial summaries and reports");


        // Add tabbedPane to the frame
        add(tabbedPane, BorderLayout.CENTER);
    }

    // Optional: Getters for panels if needed externally, though typically not.
    public TransactionPanel getTransactionPanel() {
        return transactionPanel;
    }

    public AccountPanel getAccountPanel() {
        return accountPanel;
    }

    public SummaryPanel getSummaryPanel() {
        return summaryPanel;
    }
}