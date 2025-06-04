package src; // Assuming your top-level package is 'src' or adjust as needed

import javax.swing.SwingUtilities;

import src.controller.AccountController;
import src.controller.CategoryController;
import src.controller.TransactionController;
import src.view.MainFrame; // We'll create this next

public class KysApp {

    public static void main(String[] args) {
        // It's good practice to initialize Swing UIs on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Set a clean look and feel if desired, though we are aiming for simple monochrome.
                // For now, we'll use the cross-platform look and feel (Metal) by default.
                // If you want to enforce a specific one:
                try {
                    // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    // Or try to set specific properties for a monochrome theme if simple enough
                    // UIManager.put("Panel.background", Color.WHITE);
                    // UIManager.put("Label.foreground", Color.BLACK);
                    // ... more specific UIManager settings for monochrome can be added here or in MainFrame
                } catch (Exception e) {
                    // If L&F setting fails, it will fall back to default, which is fine.
                    System.err.println("Could not set Look and Feel: " + e.getMessage());
                }

                // Instantiate controllers
                TransactionController transactionController = new TransactionController();
                AccountController accountController = new AccountController();
                CategoryController categoryController = new CategoryController();

                // Create and display the main application frame
                MainFrame mainFrame = new MainFrame(transactionController, accountController, categoryController);
                mainFrame.setVisible(true);
            }
        });
    }
}