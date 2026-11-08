package com.krisapps.pantry;

import com.krisapps.pantry.controllers.DashboardController;
import com.krisapps.pantry.controllers.InventoryManagementController;
import com.krisapps.pantry.controllers.PantryModuleController;
import com.krisapps.pantry.controllers.UpdateInventoryController;
import com.krisapps.pantry.types.Module;
import com.krisapps.pantry.util.DataManager;
import com.krisapps.pantry.util.Logging;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class PantryController {
    public final static ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);
    public final DataManager data = DataManager.getInstance();

    /* <editor-fold desc="FXML declarations"> */

    @FXML
    private StackPane root;


    /* </editor-fold> */

    final HashMap<Module, Pair<Parent, PantryModuleController>> loadedSections = new HashMap<>();
    final Logging logger = Logging.getInstance();

    private static PantryController instance;

    public PantryController() {
        if (instance == null) {
            instance = this;
        }
    }

    public static PantryController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        preloadFXML();
        initUI();
        initModule(Module.DASHBOARD);
    }

    private void preloadFXML() {
        logger.info("Preloading FXML...", "FXML");
        for (Module module: Module.values()) {
            if (module.getLayoutPath().isEmpty()) continue;
            loadedSections.put(module, loadView(module));
        }
        logger.info("Done.", "FXML");
    }

    private Pair<Parent, PantryModuleController> loadView(Module view) {
        try {
            FXMLLoader loader = new FXMLLoader(PantryApplication.class.getResource("layouts/sections/" + view.getLayoutPath()));
            switch (view) {
                case DASHBOARD -> {
                    loader.setController(new DashboardController());
                }
                case INVENTORY_MANAGEMENT -> {
                    loader.setController(new InventoryManagementController());
                }
                case INVENTORY_UPDATE -> {
                    loader.setController(new UpdateInventoryController());
                }
                case PLANOGRAM -> { }
                case GROUP_MANAGEMENT -> { }
                case EXPIRATION_DATES -> { }
                case ANALYSIS -> { }
            }

            if (loader.getController() != null) {
                ((PantryModuleController)loader.getController()).onModuleLoad();
            }
            return new Pair<>(loader.load(), loader.getController());
        } catch (Exception e) {
            DataManager.log("Failed to preload section " + view.name() + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void initModule(Module module) {
        Pair<Parent, PantryModuleController> m = loadedSections.getOrDefault(module, loadView(module));
        root.getChildren().setAll(m.getKey());
        m.getValue().onModuleOpen();
    }

    private void initUI() {

    }

    private void refreshUI() {

    }
}
