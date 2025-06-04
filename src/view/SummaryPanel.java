package src.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Import controllers if it needs to fetch data for summaries
// import src.controller.TransactionController;

public class SummaryPanel extends JPanel {

    // private TransactionController transactionController; // Example

    public SummaryPanel(/* pass controllers if needed */) {
        // this.transactionController = transactionController;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Financial Summary", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);

        JTextArea summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
        summaryArea.setText("\n\n   Summary details and charts will appear here in future updates." +
                            "\n\n   - Total Income: [Amount]" +
                            "\n   - Total Expenses: [Amount]" +
                            "\n   - Net Balance: [Amount]");
        summaryArea.setBackground(Color.WHITE);
        summaryArea.setForeground(Color.DARK_GRAY);
        add(new JScrollPane(summaryArea), BorderLayout.CENTER); // Put in scroll pane just in case

        // TODO: Add logic to fetch and display actual summary data using controllers
    }

    // public void refreshSummary() {
    // TODO: Fetch data from controllers and update the summaryArea text
    // Example:
    // try {
    // BigDecimal totalIncome = transactionController.getTotalIncome(); // Needs method in controller
    // BigDecimal totalExpenses = transactionController.getTotalExpenses(); // Needs method in controller
    // summaryArea.setText("Total Income: " + totalIncome + "\nTotal Expenses: " + totalExpenses);
    // } catch (SQLException e) {
    // summaryArea.setText("Error loading summary data.");
    // e.printStackTrace();
    // }
    // }
}