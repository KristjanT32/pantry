package com.krisapps.pantry.types;

public enum ProgramSection {
    DASHBOARD("dashboard.fxml"),
    INVENTORY_MANAGEMENT("management.fxml"),
    INVENTORY_UPDATE("update.fxml"),
    PLANOGRAM(""),
    GROUP_MANAGEMENT(""),
    EXPIRATION_DATES(""),
    ANALYSIS(""),


    ;
    private String layoutPath;

    ProgramSection(String layoutPath) {
        this.layoutPath = layoutPath;
    }

    public String getLayoutPath() {
        return layoutPath;
    }
}
