module one_empty3_apps_mylittlesynth {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media;
    requires java.desktop;
    requires java.logging;
    requires java.sql;
    requires com.miglayout.swing;
    requires com.miglayout.core;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens one.empty3.apps.mylittlesynth to javafx.fxml;
    exports one.empty3.apps.mylittlesynth;
    exports one.empty3.apps.mylittlesynth.rythms;
    exports one.empty3.apps.mylittlesynth.microphone;
    exports one.empty3.apps.mylittlesynth.processor;
}
