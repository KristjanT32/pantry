package com.krisapps.pantry.dialogs;

import com.krisapps.pantry.types.PantryDialog;
import com.krisapps.pantry.types.data.inventory.ItemType;
import com.krisapps.pantry.types.data.inventory.PantryItem;
import com.krisapps.pantry.util.PopupManager;
import com.krisapps.pantry.util.misc.Formats;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class CreateItemDialog extends PantryDialog<PantryItem> {


    @FXML
    private TextField nameField;

    @FXML
    private TextField descriptionField;

    @FXML
    private ComboBox<ItemType> typeSelector;

    @FXML
    private TextField unitsPerPackField;

    @FXML
    private TextField ownedUnitsField;

    @FXML
    private TextField preferredMinimumField;

    @FXML
    private TextField singularUnitField;

    @FXML
    private TextField pluralUnitField;

    @FXML
    private CheckBox noExpirationCheckbox;

    @FXML
    private DatePicker expirationDatePicker;

    @FXML
    private HBox expirationDateSection;

    @FXML
    private Label locationLabel;

    @FXML
    private Button pickLocationButton;

    private boolean canClose = true;

    public CreateItemDialog() {
        super("add-inventory-item.fxml", "Create new item entry", "edit_96.png");
        getDialogPane().getButtonTypes().setAll(
                new ButtonType("Create", ButtonBar.ButtonData.APPLY),
                new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        ownedUnitsField.setText("0.00");
        unitsPerPackField.setText("1.00");
        preferredMinimumField.setText("1.00");

        setResultConverter((button) -> {
            if (button.getButtonData().equals(ButtonBar.ButtonData.APPLY)) {

                if (nameField.getText().isEmpty()) {
                    PopupManager.showPopup("Item name required", "Please supply an item name to create a new inventory item.", Alert.AlertType.ERROR);
                    canClose = false;
                    return null;
                }

                if (unitsPerPackField.getText().isEmpty()) {
                    PopupManager.showPopup("Unit count per pack required", "Please supply the quantity (in units) per pack.", Alert.AlertType.ERROR);
                    canClose = false;
                    return null;
                }

                if (preferredMinimumField.getText().isEmpty()) {
                    PopupManager.showPopup("Preferred minimum quantity required", "Please supply the preferred minimum quantity for the item you are trying to create.", Alert.AlertType.ERROR);
                    canClose = false;
                    return null;
                }

                if (!noExpirationCheckbox.isSelected() && expirationDatePicker.getValue() == null) {
                    PopupManager.showPopup("Expiration date required", "Please supply the expiration date to create a new inventory item.", Alert.AlertType.ERROR);
                    canClose = false;
                    return null;
                }


                return new PantryItem(
                        -1,
                        nameField.getText(),
                        descriptionField.getText(),
                        Double.parseDouble(ownedUnitsField.getText()),
                        Double.parseDouble(unitsPerPackField.getText()),
                        Double.parseDouble(preferredMinimumField.getText()),
                        noExpirationCheckbox.isSelected() ? null : expirationDatePicker.getValue(),
                        null,
                        0d,
                        -1 /* TODO: Change to actual logic when locations are implemented */,
                        typeSelector.getValue() != null ? typeSelector.getValue() : ItemType.GENERIC
                );
            }
            return null;
        });

        setOnCloseRequest(req -> {
            if (!canClose) {
                req.consume();
                canClose = true;
            }
        });

        initUI();
    }

    private void initUI() {
        unitsPerPackField.setTextFormatter(new TextFormatter<>(Formats.NUMBERS_ONLY_FORMATTER));
        ownedUnitsField.setTextFormatter(new TextFormatter<>(Formats.NUMBERS_ONLY_FORMATTER));
        preferredMinimumField.setTextFormatter(new TextFormatter<>(Formats.NUMBERS_ONLY_FORMATTER));

        expirationDateSection.managedProperty().bind(expirationDateSection.visibleProperty());
        expirationDateSection.visibleProperty().bind(noExpirationCheckbox.selectedProperty().not());

        pickLocationButton.setOnAction(_ -> {
            PopupManager.showPredefinedPopup(PopupManager.PopupType.NOT_IMPLEMENTED);
        });
    }
}
