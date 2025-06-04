package src;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf; // Or other FlatLaf themes like FlatDarkLaf, FlatIntelliJLaf, etc.

import src.view.MainFrame;

public class Main {
    public static void main(String[] args) {
        try {
            // Set FlatLaf look and feel
            UIManager.setLookAndFeel(new FlatLightLaf());
            // For a dark theme, you could use:
            // UIManager.setLookAndFeel(new FlatDarkLaf());
            // Or an IntelliJ-like theme:
            // UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize FlatLaf: " + e.getMessage());
            // Fallback to default L&F or handle error
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}