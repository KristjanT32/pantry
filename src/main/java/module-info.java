module com.krisapps.pantry {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires com.google.gson;
    requires java.management;
    requires org.jetbrains.annotations;
    requires java.desktop;

    opens com.krisapps.pantry to javafx.fxml;
    opens com.krisapps.pantry.controllers to javafx.fxml;
    opens com.krisapps.pantry.dialogs.generic to javafx.fxml;
    opens com.krisapps.pantry.dialogs to javafx.fxml;

    exports com.krisapps.pantry.util;
    exports com.krisapps.pantry.controllers;
    exports com.krisapps.pantry;
}