package com.krisapps.pantry.dialogs;

import com.krisapps.pantry.PantryApplication;
import com.krisapps.pantry.types.PantryDialog;
import com.krisapps.pantry.util.Logging;
import com.krisapps.pantry.util.PopupManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class AboutDialog extends PantryDialog<Void> {

    @FXML
    private Label versionLabel;

    @FXML
    private Hyperlink githubLink;

    private final String GITHUB_LINK = "https://github.com/KristjanT32/income-utility-v2";


    public AboutDialog() {
        super("about.fxml", "About", "income_utility.png");

        getDialogPane().getButtonTypes().clear();
        getDialogPane().getButtonTypes().setAll(new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE));

        Package pkg = PantryApplication.class.getPackage();
        String version = pkg.getImplementationVersion();

        if (version == null) {
            version = "(dev-mode)";
        } else {
            version = "v" + version;
        }

        versionLabel.setText("Income Utility %s • Part of KrisApps Productivity Suite.".formatted(version));
        githubLink.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(URI.create(GITHUB_LINK));
            } catch (IOException e) {
                PopupManager.showPopup("Couldn't navigate to the link", "Sorry, but somehow, you can't visit links. Bummer.", Alert.AlertType.ERROR);
                Logging.getInstance().logStackTrace(e);
            }
        });
    }
}
