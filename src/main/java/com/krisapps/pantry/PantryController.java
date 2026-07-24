package com.krisapps.pantry;

import com.krisapps.pantry.types.ProgramSection;
import com.krisapps.pantry.util.DataManager;
import com.krisapps.pantry.util.Logging;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

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

    final HashMap<ProgramSection, Parent> loadedSections = new HashMap<>();
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
        switchToSection(ProgramSection.DASHBOARD);
    }

    private void preloadFXML() {
        logger.info("Preloading FXML...", "FXML");
        for (ProgramSection section: ProgramSection.values()) {
            if (section.getLayoutPath().isEmpty()) continue;
            loadedSections.put(section, loadView(section));
        }
        logger.info("Done.", "FXML");
    }

    private Parent loadView(ProgramSection view) {
        try {
            return FXMLLoader.load(PantryApplication.class.getResource("layouts/sections/" + view.getLayoutPath()));
        } catch (IOException e) {
            DataManager.log("Failed to preload section " + view.name() + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void switchToSection(ProgramSection section) {
        root.getChildren().setAll(loadedSections.getOrDefault(section, loadView(section)));
    }

    private void initUI() {

    }

    private void refreshUI() {

    }
}
