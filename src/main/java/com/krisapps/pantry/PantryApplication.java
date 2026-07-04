package com.krisapps.pantry;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PantryApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PantryApplication.class.getResource("layouts/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 460);
        stage.setTitle("Pantry");
        stage.setScene(scene);
        stage.show();
    }
}
