module com.krisapps.pantry {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires com.google.gson;
    requires java.management;
    requires org.jetbrains.annotations;
    requires java.desktop;
    requires org.kordamp.ikonli.javafx;

    opens com.krisapps.pantry to javafx.fxml;
    opens com.krisapps.pantry.controllers to javafx.fxml;
    opens com.krisapps.pantry.dialogs.generic to javafx.fxml;
    opens com.krisapps.pantry.dialogs to javafx.fxml;
    opens com.krisapps.pantry.types.data to com.google.gson;

    exports com.krisapps.pantry.util;
    exports com.krisapps.pantry.controllers;
    exports com.krisapps.pantry;
    opens com.krisapps.pantry.types.data.inventory to com.google.gson;
}