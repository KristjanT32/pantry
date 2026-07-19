package com.krisapps.pantry.controllers;

import com.krisapps.pantry.PantryController;
import com.krisapps.pantry.types.ProgramSection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class InventoryManagementController {

    @FXML
    private Button backButton;

    private PantryController primaryController;

    @FXML
    public void initialize() {
        primaryController = PantryController.getInstance();

        backButton.setOnAction(_ -> {
            primaryController.switchToSection(ProgramSection.DASHBOARD);
        });
    }
}
