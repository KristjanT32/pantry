package com.krisapps.pantry.types;

import com.krisapps.pantry.PantryApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class PantryDialog<T> extends Dialog<T> {

    protected VBox rootPane;

    /**
     * Constructs a generic PantryDialog.
     *
     * @param dialogFileName The name of the dialog file (with extension), relative to the <code>dialogs</code> folder in the resource directory.
     * @param title          The title of the dialog window.
     */
    public PantryDialog(String dialogFileName, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(PantryApplication.class.getResource("layouts/dialogs/%s".formatted(dialogFileName)));
            loader.setController(this);
            rootPane = loader.load();
            getDialogPane().getStylesheets().add(PantryApplication.class.getResource("stylesheets/core-ui.css").toExternalForm());
            getDialogPane().getStylesheets().add(PantryApplication.class.getResource("stylesheets/main.css").toExternalForm());

            getDialogPane().setContent(rootPane);
            ((Stage) getDialogPane().getScene().getWindow()).getIcons().add(new Image(PantryApplication.class.getResource("icons/pantry_96.png").toExternalForm()));

            getDialogPane().getButtonTypes().clear();
            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            getDialogPane().getButtonTypes().add(closeButton);

            Node b = getDialogPane().lookupButton(closeButton);
            b.setVisible(false);
            b.setManaged(false);

            initModality(Modality.APPLICATION_MODAL);
            setTitle(title);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructs a generic PantryDialog.
     * @param dialogFileName The name of the dialog file (with extension), relative to the <code>dialogs</code> folder in the resource directory.
     * @param title The title of the dialog window.
     * @param iconFileName The name of the icon file (with extension), relative to the <code>icons</code> folder in the resource directory.
     */
    public PantryDialog(String dialogFileName, String title, String iconFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(PantryApplication.class.getResource("layouts/dialogs/%s".formatted(dialogFileName)));
            loader.setController(this);
            rootPane = loader.load();
            getDialogPane().getStylesheets().add(PantryApplication.class.getResource("stylesheets/core-ui.css").toExternalForm());
            getDialogPane().getStylesheets().add(PantryApplication.class.getResource("stylesheets/main.css").toExternalForm());

            getDialogPane().setContent(rootPane);
            ((Stage) getDialogPane().getScene().getWindow()).getIcons().add(new Image(PantryApplication.class.getResource("icons/" + iconFileName).toExternalForm()));

            getDialogPane().getButtonTypes().clear();
            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            getDialogPane().getButtonTypes().add(closeButton);

            Node b = getDialogPane().lookupButton(closeButton);
            b.setVisible(false);
            b.setManaged(false);

            initModality(Modality.APPLICATION_MODAL);
            setTitle(title);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setAllowResize(boolean allowResize) {
        ((Stage) getDialogPane().getScene().getWindow()).setResizable(allowResize);
    }
}
