package com.krisapps.pantry;

import com.krisapps.pantry.util.DataManager;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class PantryController {
    public final static ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);
    public final DataManager data = DataManager.getInstance();

    /* <editor-fold desc="FXML declarations"> */

    @FXML
    private VBox updateTile;

    @FXML
    private VBox overviewTile;


    @FXML
    private VBox inventoryView;

    @FXML
    private VBox updateView;

    @FXML
    private VBox dashboardView;

    /* </editor-fold> */




    @FXML
    public void initialize() {
        initUI();
    }

    private void initUI() {
        dashboardView.setVisible(true);
        inventoryView.setVisible(false);
        updateView.setVisible(false);

        updateTile.setOnMouseClicked(_ -> {
            dashboardView.setVisible(false);
            inventoryView.setVisible(false);
            updateView.setVisible(true);
        });

        overviewTile.setOnMouseClicked(_ -> {
            inventoryView.setVisible(true);
            dashboardView.setVisible(false);
            updateView.setVisible(false);
        });
    }

    private void refreshUI() {

    }

}
