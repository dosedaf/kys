package src.view;

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import src.controller.AccountController;

public class AccountManagementDialog extends JDialog {
    public AccountManagementDialog(Frame owner, AccountController accountController) {
        super(owner, "Manage Accounts", true); // Modal
        setSize(400, 300);
        setLocationRelativeTo(owner);
        add(new JLabel("Account Management - To be implemented", SwingConstants.CENTER));
        // Similar to CategoryManagementDialog, would contain UI for account CRUD.
    }
}