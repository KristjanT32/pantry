package com.krisapps.pantry.ui.listview.cell;

import com.krisapps.pantry.PantryApplication;
import com.krisapps.pantry.types.data.inventory.PantryItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

public class ReadOnlyInventoryItemCell extends ListCell<PantryItem> {

    @FXML
    private VBox rootPane;

    @FXML
    private Label itemNameLabel;

    @FXML
    private Label itemDescriptionLabel;

    @FXML
    private Label itemLocationLabel;

    @FXML
    private Label itemQuantityLabel;

    @FXML
    private FontIcon stockStatusIcon;

    public ReadOnlyInventoryItemCell() {
        loadFXML();
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(PantryApplication.class.getResource("layouts/ui/pantry_item_cell_readonly.fxml"));
            loader.setController(this);
            rootPane = loader.load();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(PantryItem item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty) {
            setGraphic(rootPane);

            itemNameLabel.setText(item.name());
            itemNameLabel.setStyle("-fx-text-fill: black");

            itemDescriptionLabel.setText(item.description());
            itemDescriptionLabel.setStyle("-fx-text-fill: black");

            itemLocationLabel.setText(item.location() != null ? item.location().name() : "No location");
            itemLocationLabel.setStyle("-fx-text-fill: black");

            itemQuantityLabel.setText(item.quantity() + " / " + item.preferredMinimumQuantity());

            if (item.quantity() >= item.preferredMinimumQuantity()) {
                stockStatusIcon.setIconLiteral("fltral-checkmark-circle-24");
                stockStatusIcon.setFill(Paint.valueOf("#00e034"));
                itemQuantityLabel.setTextFill(Paint.valueOf("#00e034"));
            } else if (item.quantity() < item.preferredMinimumQuantity() && item.quantity() > 0) {
                stockStatusIcon.setIconLiteral("fltral-circle-half-fill-24");
                stockStatusIcon.setFill(Paint.valueOf("#db8b14"));
                itemQuantityLabel.setTextFill(Paint.valueOf("#db8b14"));
            } else {
                stockStatusIcon.setIconLiteral("fltral-error-circle-24");
                stockStatusIcon.setFill(Paint.valueOf("#e00000"));
                itemQuantityLabel.setTextFill(Paint.valueOf("#e00000"));
            }

            rootPane.prefWidthProperty().bind(getListView().widthProperty().subtract(20));
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}
