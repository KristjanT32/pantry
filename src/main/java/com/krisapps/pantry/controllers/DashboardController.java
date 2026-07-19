package com.krisapps.pantry.controllers;

import com.krisapps.pantry.PantryController;
import com.krisapps.pantry.types.ProgramSection;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class DashboardController {

    @FXML
    private VBox updateTile;

    @FXML
    private VBox overviewTile;

    private PantryController primaryController;


    @FXML
    public void initialize() {
        primaryController = PantryController.getInstance();
        initUI();
    }

    private void initUI() {
        updateTile.setOnMouseClicked(_ -> {
            primaryController.switchToSection(ProgramSection.INVENTORY_UPDATE);
        });

        overviewTile.setOnMouseClicked(_ -> {
            primaryController.switchToSection(ProgramSection.INVENTORY_MANAGEMENT);
        });
    }
}
