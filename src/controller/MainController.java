package src.controller;

import src.view.DashboardView;

public class MainController {
    private DashboardView dashboard;

    public void init() {
        dashboard = new DashboardView();
        dashboard.setVisible(true);
    }
}
