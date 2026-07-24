package com.krisapps.pantry.controllers;

import com.krisapps.pantry.PantryController;
import com.krisapps.pantry.types.ProgramSection;
import com.krisapps.pantry.util.DataManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class DashboardController {

    @FXML
    private VBox updateTile;

    @FXML
    private VBox overviewTile;

    @FXML
    private Label lastUpdateLabel;

    @FXML
    private Label lastUpdateDateLabel;

    @FXML
    private Label primaryNotificationLabel;

    @FXML
    private FontIcon primaryNotificationIcon;

    @FXML
    private VBox primaryNotificationPanel;

    private PantryController primaryController;
    private final DataManager data = DataManager.getInstance();


    @FXML
    public void initialize() {
        primaryController = PantryController.getInstance();
        initUI();
        refreshUI();
    }

    private void initUI() {
        updateTile.setOnMouseClicked(_ -> {
            primaryController.switchToSection(ProgramSection.INVENTORY_UPDATE);
        });

        overviewTile.setOnMouseClicked(_ -> {
            primaryController.switchToSection(ProgramSection.INVENTORY_MANAGEMENT);
        });

        lastUpdateDateLabel.managedProperty().bind(lastUpdateDateLabel.visibleProperty());
        lastUpdateDateLabel.visibleProperty().bind(lastUpdateDateLabel.textProperty().length().greaterThan(0));
    }

    private void refreshUI() {
        LocalDateTime lastUpdate = data.getConfigurationData().getLastInventoryUpdate();
        if (lastUpdate != null) {
            Duration duration = Duration.between(LocalDateTime.now(), lastUpdate).abs();

            lastUpdateLabel.setText("Last inventory update " + (duration.toDays() <= 99 ? duration.toDays() : "99+") + " days ago");
            lastUpdateDateLabel.setText("Updated on " + lastUpdate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " at " + lastUpdate.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        } else {
            lastUpdateLabel.setText("No inventory updates yet");
            lastUpdateDateLabel.setText("");
        }
    }

    private void clearPrimaryNotification() {
        Platform.runLater(() -> {
            setPrimaryNotification("", false);
        });
    }

    private void setPrimaryNotification(String notificationText, boolean isWarning) {
        Platform.runLater(() -> {
            if (notificationText.trim().isEmpty()) {
                primaryNotificationLabel.setText("You have no notifications. Everything is fine!");
                primaryNotificationIcon.setIconLiteral("fltfal-checkmark-circle-24");
                primaryNotificationPanel.getStyleClass().setAll("notification-highlight-clear", "notification-panel-secondary");
            } else {
                primaryNotificationLabel.setText(notificationText);
                primaryNotificationIcon.setIconLiteral(isWarning ? "fltfmz-warning-24" : "fltfal-info-24");
                primaryNotificationPanel.getStyleClass().setAll(isWarning ? "notification-highlight-warning" : "notification-highlight-info", "notification-panel-secondary");
            }
        });
    }
}
