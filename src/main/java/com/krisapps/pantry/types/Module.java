package com.krisapps.pantry.types;

public enum Module {
    DASHBOARD("dashboard.fxml"),
    INVENTORY_MANAGEMENT("management.fxml"),
    INVENTORY_UPDATE("update.fxml"),
    PLANOGRAM(""),
    GROUP_MANAGEMENT(""),
    EXPIRATION_DATES(""),
    ANALYSIS(""),


    ;
    private String layoutPath;

    Module(String layoutPath) {
        this.layoutPath = layoutPath;
    }

    public String getLayoutPath() {
        return layoutPath;
    }
}
