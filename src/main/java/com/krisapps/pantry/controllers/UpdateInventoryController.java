package com.krisapps.pantry.controllers;

import com.krisapps.pantry.PantryController;
import com.krisapps.pantry.dialogs.CreateItemDialog;
import com.krisapps.pantry.types.Module;
import com.krisapps.pantry.types.data.inventory.PantryItem;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

import java.util.Optional;

public class UpdateInventoryController implements PantryModuleController {

    @FXML
    private Button backButton;

    @FXML
    private MenuItem addItemButton;

    @FXML
    private MenuItem selectItemButton;

    private PantryController primaryController;

    @FXML
    public void initialize() {
        primaryController = PantryController.getInstance();

        backButton.setOnAction(_ -> {
            primaryController.initModule(Module.DASHBOARD);
        });

        addItemButton.setOnAction(_ -> {
            CreateItemDialog dialog = new CreateItemDialog();
            Optional<PantryItem> item = dialog.showAndWait();

            item.ifPresent(pantryItem -> primaryController.data.addInventoryItem(pantryItem));
        });
    }

    @Override
    public void onModuleLoad() {

    }

    @Override
    public void onModuleOpen() {

    }

    @Override
    public void onModuleClose() {

    }
}
