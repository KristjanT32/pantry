module com.krisapps.pantry {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires java.sql;
    requires com.google.gson;
    requires java.management;
    requires org.jetbrains.annotations;

    opens com.krisapps.pantry to javafx.fxml;
    exports com.krisapps.pantry;
}