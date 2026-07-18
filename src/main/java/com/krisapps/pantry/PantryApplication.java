package com.krisapps.pantry;

import com.krisapps.pantry.dialogs.generic.LoadingDialog;
import com.krisapps.pantry.util.DataManager;
import com.krisapps.pantry.util.PopupManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PantryApplication extends Application {

    static Stage window;

    /**
     * Updates the window title.
     *
     * @param title        The text to set the window title to.
     * @param removePrefix If <code>true</code>, 'KrisApps Income Utility: ' will not be appended to the beginning of the title.
     */
    public static void updateTitle(String title, boolean removePrefix) {
        if (window == null) return;
        window.setTitle(removePrefix ? title : "KrisApps Pantry: " + title);
    }


    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PantryApplication.class.getResource("layouts/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 870);
        stage.setTitle("Pantry");
        stage.setScene(scene);
        stage.getIcons().add(new Image(PantryApplication.class.getResource("icons/pantry_96.png").toExternalForm()));

        window = stage;
        window.setMinWidth(1300);
        window.setMinHeight(920);
        window.setOnCloseRequest((event) -> {
            PopupManager.showConfirmation(
                    "Shutdown application",
                    "Are you sure you wish to close the application?",
                    new ButtonType("Yes", ButtonBar.ButtonData.APPLY),
                    new ButtonType("No, cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
            ).ifPresent(response -> {
                if (response.getButtonData() == ButtonBar.ButtonData.APPLY) {
                    shutdown();
                } else {
                    event.consume();
                }
            });
        });
        stage.show();
    }

    public static void restart() {
        try {
            String javaBin = System.getProperty("java.home")
                    + File.separator + "bin"
                    + File.separator + "java";

            File currentJar = new File(
                    PantryApplication.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            if (!currentJar.getName().endsWith(".jar")) {
                DataManager.log("Restarting is not possible, as the application is not run from a .jar file.");
                return;
            }

            List<String> command = new ArrayList<>();
            command.add(javaBin);
            command.add("-jar");
            command.add(currentJar.getPath());

            new ProcessBuilder(command).start();

            shutdown();
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        LoadingDialog dialog = new LoadingDialog(LoadingDialog.LoadingOperationType.INDETERMINATE_PROGRESSBAR);
        dialog.setPrimaryLabel("Cleaning up");
        dialog.show("Shutting down...", () -> {
            DataManager.getInstance().saveCurrentConfigurationData();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            while (DataManager.getInstance().isSaving()) {
                dialog.setPrimaryLabel("Saving data");
                dialog.setSecondaryLabel("Waiting for I/O operations to finish...");
            }

            dialog.setPrimaryLabel("Resolving tension");
            dialog.setSecondaryLabel("Closing, bye!");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Platform.exit();
            System.exit(0);
        });
    }
}
