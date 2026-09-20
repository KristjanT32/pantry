package com.krisapps.pantry.ui.combobox.cell;

import com.krisapps.pantry.PantryApplication;
import com.krisapps.pantry.types.data.inventory.ItemCategory;
import com.krisapps.pantry.types.data.inventory.ItemGroup;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ItemCategoryComboboxCell extends ListCell<ItemCategory> {

    @FXML
    private VBox rootPane;

    @FXML
    private Label nameLabel;

    @FXML
    private Label descriptionLabel;

    public ItemCategoryComboboxCell() {
        loadFXML();
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(PantryApplication.class.getResource("layouts/ui/category_combobox_cell.fxml"));
            loader.setController(this);
            rootPane = loader.load();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(ItemCategory item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty) {
            setGraphic(rootPane);

            nameLabel.setText(item.name());
            nameLabel.setStyle("-fx-text-fill: black");

            descriptionLabel.setText(item.description());
            descriptionLabel.setStyle("-fx-text-fill: black");
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}
