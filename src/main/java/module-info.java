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
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires kotlin.stdlib;
    requires kotlinx.serialization.core;
    requires kotlinx.serialization.json;

    opens one.empty3.apps.mylittlesynth to javafx.fxml;

    exports one.empty3.apps.mylittlesynth;
    exports one.empty3.apps.mylittlesynth.rythms;
    exports one.empty3.apps.mylittlesynth.microphone;
    exports one.empty3.apps.mylittlesynth.processor;
    exports one.empty3.audio.sampler.core;
    exports one.empty3.audio.sampler.dsp;
    exports one.empty3.audio.sampler.dsp.fft;
    exports one.empty3.audio.sampler.dsp.stft;
    exports one.empty3.audio.sampler.dsp.model;
    exports one.empty3.audio.sampler.dsp.analyzer;
    exports one.empty3.audio.sampler.dsp.transform;
    exports one.empty3.audio.sampler.dsp.vocoder;
    exports one.empty3.audio.sampler.dsp.synth;
}
