package com.krisapps.pantry.controllers;

import com.krisapps.pantry.PantryController;
import com.krisapps.pantry.dialogs.generic.LoadingDialog;
import com.krisapps.pantry.types.ProgramSection;
import com.krisapps.pantry.types.data.inventory.PantryItem;
import com.krisapps.pantry.ui.listview.ReadOnlyInventoryItemCellFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public class InventoryManagementController {

    @FXML
    private Button backButton;

    @FXML
    private ListView<PantryItem> inventoryList;

    private PantryController primaryController;

    @FXML
    public void initialize() {
        primaryController = PantryController.getInstance();

        backButton.setOnAction(_ -> {
            primaryController.switchToSection(ProgramSection.DASHBOARD);
        });

        initUI();
        initData();
    }

    private void initUI() {
        inventoryList.setCellFactory(new ReadOnlyInventoryItemCellFactory());
    }

    private void initData() {
        LoadingDialog loadingDialog = new LoadingDialog(LoadingDialog.LoadingOperationType.INDETERMINATE_PROGRESSBAR);
        loadingDialog.setPrimaryLabel("Please wait");
        loadingDialog.setSecondaryLabel("Loading inventory data...");
        loadingDialog.show("Hold on a sec", () -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            inventoryList.getItems().setAll(primaryController.data.getItems());
            inventoryList.refresh();
        });
    }
}
