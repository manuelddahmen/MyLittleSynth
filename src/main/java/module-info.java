module org.project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires java.desktop;
    requires miglayout;

    opens org.project to javafx.fxml;
    exports one.empty3.apps.mylittlesynth.rythms;
}