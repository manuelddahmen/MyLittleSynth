package one.empty3.apps.mylittlesynth;

import one.empty3.apps.mylittlesynth.processor.WaveForm;
import one.empty3.apps.mylittlesynth.rythms.RythmPanel;
import one.empty3.audio.sampler.core.AdsrSettings;
import one.empty3.audio.sampler.core.DesktopAudioOutput;
import one.empty3.audio.sampler.core.SampleData;
import one.empty3.audio.sampler.core.Sampler;
import one.empty3.audio.sampler.core.SamplerInstrument;
import one.empty3.audio.sampler.dsp.FourierEngine;
import one.empty3.audio.sampler.dsp.analyzer.SignalAnalyzer;
import one.empty3.audio.sampler.dsp.model.SpectralModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.*;

public class App extends Application {
    private Slider slider;
    private SoundProductionSystem soundProductionSystem;
    private ToggleGroup group;
    private Player player;
    Map noteMap;
    private double minDurationSec = 4.0D;
    private Slider volume;
    private Button[] buttons;
    private int[] buttonNo;
    private String[] baseButtonTexts;

    private Stage primaryStage;
    private Accordion accordion;
    private TitledPane synthTitledPane;
    private TitledPane fourierTitledPane;
    private TitledPane midiGridTitledPane;
    private FourierPanel fourierPanel;
    private MidiGridPanel midiGridPanel;
    private Button recordButton;
    private Canvas waveformCanvas;
    private CheckBox showSpectrumCheck;
    private SampleData lastVisualSample;
    private SpectralModel lastVisualSpectrum;
    private String lastVisualLabel;

    private Sampler sampler;
    private DesktopAudioOutput samplerAudioOutput;
    private Label samplerStatusLabel;
    private RadioButton samplerRadioButton;
    private final Set<Button> activePressedButtons = new HashSet<>();
    private final Map<Integer, String> assignedNoteFiles = new HashMap<>();
    private String rootSampleFileName = null;
    private int rootSampleMidi = -1;

    public SoundProductionSystem getSoundProductionSystem() {
        return this.soundProductionSystem;
    }

    public void setSoundProductionSystem(SoundProductionSystem soundProductionSystem) {
        this.soundProductionSystem = soundProductionSystem;
    }

    public Sampler getSampler() {
        return this.sampler;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.setSoundProductionSystem(new SoundProductionSystem(60.0F));
        this.sampler = new Sampler(null, 44100, 16);
        this.samplerAudioOutput = new DesktopAudioOutput(this.sampler, 44100, 256);
        this.samplerAudioOutput.setPcmListener((data, len) ->
                this.getSoundProductionSystem().writeWaveBuffer(data, len));

        Parent root = new AnchorPane();
        root.minWidth(800.0D);
        root.minHeight(600.0D);
        Scene scene = new Scene(root);
        this.buttons = new Button[16];
        this.buttonNo = new int[16];
        HBox[] pane = new HBox[]{new HBox(), new HBox()};
        BorderPane bp = new BorderPane();
        BorderPane bp2 = new BorderPane();
        bp.setTop(pane[0]);
        bp.setCenter(pane[1]);
        String[] notes = new String[]{"do#", "re#", "--", "fa#", "sol#", "la#", "--", "do#", "do", "re", "mi", "fa", "sol", "la", "si", "do"};
        this.baseButtonTexts = notes.clone();
        Canvas canvas = new Canvas(640.0D, 260.0D);
        this.waveformCanvas = canvas;
        SamplerWizardDialog.drawWaveform(canvas, null, null,
                "Click 'WAV SAMPLER WIZARD...' to choose a WAV file & assign a note");

        Pane pane1 = new Pane();
        pane1.getChildren().add(canvas);
        this.slider = new Slider();
        this.slider.setMin(0.0D);
        this.slider.setMax(12.0D);
        this.slider.setValue(4.0D);
        this.slider.setMinorTickCount(1);
        this.slider.setAccessibleText("Octaves");
        this.slider.setOnMouseReleased((event) -> {
            int value = (int) ((Slider) event.getSource()).getValue();
            this.player.setOctave(value);
            if (this.sampler != null) {
                this.sampler.allNotesOff();
            }
            this.activePressedButtons.clear();
            this.updateButtonNoteNumbers();
            this.updateKeyboardAssignmentBadges();
            System.out.println("Octave: " + value);
        });
        this.noteMap = new HashMap();

        int i;
        for (i = 0; i < this.buttons.length; ++i) {
            this.buttons[i] = new Button(notes[i]);
            this.buttons[i].setId("Button" + i);
            if (i < 8) {
                if (notes[i].equals("--")) {
                    this.buttons[i].setStyle("-fx-background-color: transparent; -fx-text-fill: transparent; -fx-border-color: transparent;");
                    this.buttons[i].setDisable(true);
                } else {
                    this.buttons[i].setStyle(getDefaultBlackKeyStyle());
                }
                pane[0].getChildren().add(this.buttons[i]);
            } else {
                this.buttons[i].setStyle(getDefaultWhiteKeyStyle());
                pane[1].getChildren().add(this.buttons[i]);
            }
        }

        for (i = 0; i < 96; ++i) {
            this.noteMap.put(i, new Note(this.noteMap, this.minDurationSec, i, WaveForm.SIN));
        }

        KeyCode[] keycode = new KeyCode[]{KeyCode.A, KeyCode.Z, KeyCode.E, KeyCode.R, KeyCode.T, KeyCode.Y, KeyCode.U, KeyCode.I, KeyCode.Q, KeyCode.S, KeyCode.D, KeyCode.F, KeyCode.G, KeyCode.H, KeyCode.J, KeyCode.K};

        for (i = 0; i < this.buttons.length; ++i) {
            this.buttons[i].setOnMousePressed((event) -> {
                this.playNote((Button) event.getSource());
            });
            this.buttons[i].setOnMouseReleased((event) -> {
                this.stopNote((Button) event.getSource());
            });
            this.buttons[i].setOnMouseExited((event) -> {
                this.stopNote((Button) event.getSource());
            });
            this.buttons[i].setOnTouchPressed((event) -> {
                this.playNote((Button) event.getSource());
            });
            this.buttons[i].setOnTouchReleased((event) -> {
                this.stopNote((Button) event.getSource());
            });
            this.buttons[i].setPadding(new Insets(10.0D, 10.0D, 10.0D, 10.0D));
            this.buttonNo[i] = this.getTone(this.buttons[i]);
        }

        VBox vBox = new VBox(8);
        vBox.setPadding(new Insets(10));
        this.group = new ToggleGroup();
        WaveForm[] waveforms = {
                WaveForm.SIN, WaveForm.RECT, WaveForm.TRI, WaveForm.SAWTOOTH,
                WaveForm.PULSE, WaveForm.NOISE, WaveForm.ORGAN, WaveForm.SAMPLER
        };
        String[] wfNames = {
                "Sine", "Square", "Triangle", "Sawtooth",
                "Pulse", "Noise", "Organ", "Sampler (WAV / Fourier)"
        };
        for (int w = 0; w < waveforms.length; w++) {
            RadioButton rb = new RadioButton(wfNames[w]);
            rb.setToggleGroup(this.group);
            rb.setUserData(waveforms[w]);
            if (w == 0) rb.setSelected(true);
            if (waveforms[w] == WaveForm.SAMPLER) {
                this.samplerRadioButton = rb;
            }
            rb.setOnAction((event) -> {
                RadioButton source = (RadioButton) event.getSource();
                WaveForm selectedWf = (WaveForm) source.getUserData();
                this.setForm(selectedWf);
                if (selectedWf == WaveForm.SAMPLER && !this.sampler.hasAnySample()) {
                    this.openSamplerWizard();
                }
            });
            vBox.getChildren().add(rb);
        }
        vBox.getChildren().add(new Label("Octave:"));
        vBox.getChildren().add(this.slider);

        Button record = new Button("Record");
        this.recordButton = record;
        record.setStyle("-fx-font-weight: bold;");
        vBox.getChildren().add(record);
        record.setOnAction((actionEvent) -> {
            this.toggleWavRecording();
        });
        Button stop = new Button("STOP REPEAT");
        vBox.getChildren().add(stop);
        stop.setOnAction((actionEvent) -> {
            this.player.setRecording(false);
            this.player.setPlayingBuffer(false);
            if (this.getSoundProductionSystem().isRecording()) {
                this.toggleWavRecording();
            }
        });

        Button wavWizardBtn = new Button("WAV SAMPLER WIZARD...");
        wavWizardBtn.setStyle("-fx-background-color: #0969da; -fx-text-fill: white; -fx-font-weight: bold;");
        vBox.getChildren().add(wavWizardBtn);
        wavWizardBtn.setOnAction((actionEvent) -> {
            this.openSamplerWizard();
        });

        Button fourierShortcutBtn = new Button("FOURIER ENGINE...");
        fourierShortcutBtn.setStyle("-fx-background-color: #8250df; -fx-text-fill: white; -fx-font-weight: bold;");
        vBox.getChildren().add(fourierShortcutBtn);
        fourierShortcutBtn.setOnAction((actionEvent) -> {
            if (this.accordion != null && this.fourierTitledPane != null) {
                this.accordion.setExpandedPane(this.fourierTitledPane);
            }
        });

        this.showSpectrumCheck = new CheckBox("Afficher Spectre FFT");
        this.showSpectrumCheck.setOnAction(e -> refreshMainCanvas());
        vBox.getChildren().add(this.showSpectrumCheck);

        Button clearSamplesBtn = new Button("CLEAR WAV SAMPLES");
        vBox.getChildren().add(clearSamplesBtn);
        clearSamplesBtn.setOnAction((actionEvent) -> {
            this.clearSamplerAssignments();
        });

        this.samplerStatusLabel = new Label("Sampler: No WAV loaded");
        this.samplerStatusLabel.setWrapText(true);
        this.samplerStatusLabel.setMaxWidth(210);
        this.samplerStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #333333;");
        vBox.getChildren().add(this.samplerStatusLabel);

        Button roundButton = new Button();
        roundButton.setOnRotate(rotateEvent -> System.out.println("rotate" + rotateEvent.getAngle()));
        roundButton.setOnRotationFinished(rotateEvent -> System.out.println("rotation finish" + rotateEvent.getAngle()));
        vBox.getChildren().add(roundButton);

        Slider zoomSlider = new Slider();
        zoomSlider.setMin(1.0D);
        zoomSlider.setMax(20.0D);
        zoomSlider.setValue(1.0D);
        vBox.getChildren().add(zoomSlider);

        bp.setLeft(vBox);
        bp.setBottom(pane1);
        bp2.setTop(bp);
        bp2.setCenter(canvas);

        this.synthTitledPane = new TitledPane("Synth & WAV Sampler (Clavier)", bp2);
        this.synthTitledPane.setOnKeyPressed((event) -> {
            for (int i1 = 0; i1 < this.buttons.length; ++i1) {
                if (event.getCode().equals(keycode[i1])) {
                    this.playNote(this.buttons[i1]);
                }
            }
        });
        this.synthTitledPane.setOnKeyReleased((event) -> {
            for (int i2 = 0; i2 < this.buttons.length; ++i2) {
                if (event.getCode().equals(keycode[i2])) {
                    this.stopNote(this.buttons[i2]);
                }
            }
        });
        bp2.setPadding(new Insets(20.0D, 20.0D, 20.0D, 20.0D));

        TitledPane rythmTitledPane = new TitledPane();
        rythmTitledPane.setText("Rhythm & Loops");
        VBox rythmBox = new VBox();
        RythmPanel rythmPanel = new RythmPanel(rythmTitledPane, this);
        rythmBox.getChildren().addAll(new Node[]{rythmPanel});
        rythmBox.getChildren().add(rythmPanel.getCanvasTimelineProgress());
        rythmTitledPane.setContent(rythmBox);

        this.fourierPanel = new FourierPanel(this);
        this.fourierTitledPane = new TitledPane("Empty3 Fourier Engine (Timbre & Phase Vocoder)", this.fourierPanel);

        this.midiGridPanel = new MidiGridPanel(this);
        this.midiGridTitledPane = new TitledPane("MIDI Grid & Instruments (AppNew)", this.midiGridPanel);

        this.accordion = new Accordion();
        this.accordion.getPanes().addAll(this.synthTitledPane, rythmTitledPane, this.fourierTitledPane, this.midiGridTitledPane);
        scene.setRoot(this.accordion);
        this.accordion.setExpandedPane(this.synthTitledPane);

        bp2.setBottom(this.volume = new Slider());
        this.volume.setMin(0.0D);
        this.volume.setMax(100.0D);
        this.volume.setValue(100.0D);
        this.volume.setAccessibleText("Volume:" + this.volume.getValue());
        this.volume.setOnMouseReleased((event) -> {
            this.player.setVolume(this.volume.getValue());
            if (this.sampler != null) {
                this.sampler.setMasterVolume((float) (this.volume.getValue() / 100.0D));
            }
            System.out.println("Volume du player: " + this.player.getVolume());
        });
        this.player = new Player(this);
        this.player.setRecording(false);
        this.player.setVolume(100.0D);
        this.player.start();

        primaryStage.setTitle("MyLittleSynth 5.0 — Empty3 Fourier Engine");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            App.this.player.setPlaying(false);
            if (App.this.midiGridPanel != null) {
                App.this.midiGridPanel.close();
            }
            if (App.this.samplerAudioOutput != null && App.this.samplerAudioOutput.isRunning()) {
                App.this.samplerAudioOutput.stop();
            }
            App.this.getSoundProductionSystem().stopRecordingAndWrite();
            App.this.getSoundProductionSystem().end();
            App.this.getSoundProductionSystem().setEnded(true);
            System.exit(0);
        });
    }

    public void toggleWavRecording() {
        if (!this.getSoundProductionSystem().isRecording()) {
            this.getSoundProductionSystem().startRecording();
            if (this.player != null) {
                this.player.setRecording(true);
            }
            if (this.recordButton != null) {
                this.recordButton.setText("■ Stop Record");
                this.recordButton.setStyle("-fx-background-color: #cf222e; -fx-text-fill: white; -fx-font-weight: bold;");
            }
            String msg = "Enregistrement WAV en cours...";
            if (this.samplerStatusLabel != null) {
                this.samplerStatusLabel.setText(msg);
            }
            if (this.midiGridPanel != null) {
                this.midiGridPanel.updateRecordingState(true, msg);
            }
        } else {
            if (this.player != null) {
                this.player.setRecording(false);
            }
            java.io.File saved = this.getSoundProductionSystem().stopRecordingAndWrite();
            if (this.recordButton != null) {
                this.recordButton.setText("Record");
                this.recordButton.setStyle("-fx-font-weight: bold;");
            }
            String msg = saved != null
                    ? "WAV enregistré : " + saved.getName()
                    : "Enregistrement arrêté (aucun son capturé)";
            if (this.samplerStatusLabel != null) {
                this.samplerStatusLabel.setText(msg);
            }
            if (this.midiGridPanel != null) {
                this.midiGridPanel.updateRecordingState(false, msg);
            }
        }
    }

    public void playExternalNote(int tone, int midiNote, String label) {
        if (this.player == null) return;
        if (this.player.getForm() == WaveForm.SAMPLER && this.sampler != null && this.sampler.hasSampleForNote(midiNote)) {
            ensureSamplerAudioRunning();
        }
        Note note = (Note) this.noteMap.get(tone);
        if (note == null) {
            note = new Note(this.noteMap, this.minDurationSec, tone, this.player.getForm());
            this.noteMap.put(tone, note);
        }
        note.setWaveform(this.player.getForm());
        this.player.playNote(note);
    }

    public void stopExternalNote(int tone, int midiNote) {
        if (this.sampler != null) {
            this.sampler.noteOff(midiNote);
        }
        Note note = (Note) this.noteMap.get(tone);
        if (note != null && this.player != null) {
            this.player.stopNote(note);
        }
    }

    public void openSamplerWizard() {
        int currentOctave = (int) this.slider.getValue();
        SamplerWizardDialog wizard = new SamplerWizardDialog();
        Optional<SamplerWizardDialog.WizardResult> resultOpt =
                wizard.showAndWait(this.primaryStage, currentOctave);
        resultOpt.ifPresent(this::applySamplerWizardResult);
    }

    public void applySamplerWizardResult(SamplerWizardDialog.WizardResult result) {
        if (result == null) return;

        if (this.fourierPanel != null) {
            this.fourierPanel.setBaseSample(result.getSampleData(), result.getWavFile().getName());
        }

        if (result.isChromaticRootMode() && result.isUsePhaseVocoder()) {
            if (this.samplerStatusLabel != null) {
                this.samplerStatusLabel.setText("Fourier Phase Vocoder: génération des zones...");
            }
            Task<SamplerInstrument> task = new Task<>() {
                @Override
                protected SamplerInstrument call() {
                    List<Integer> anchors = FourierPanel.buildAnchorMidiNotes(result.getAssignedMidiNote(), true);
                    AdsrSettings adsr = new AdsrSettings(
                            result.getAttack(),
                            result.getDecay(),
                            result.getSustain(),
                            result.getRelease()
                    );
                    return FourierEngine.buildInstrument(
                            result.getWavFile().getName(),
                            result.getSampleData(),
                            anchors,
                            result.isPreserveFormants(),
                            0.0,
                            1.0,
                            16,
                            adsr,
                            result.getLoopRegion()
                    );
                }
            };
            task.setOnSucceeded(e -> {
                SamplerInstrument inst = task.getValue();
                float[] mono = FourierPanel.extractMono(result.getSampleData());
                int win = mono.length >= 2048 ? 2048 : 512;
                SpectralModel model = SignalAnalyzer.analyze(mono, result.getSampleData().getSampleRate(), win, win / 4);
                this.rootSampleFileName = result.getWavFile().getName();
                this.rootSampleMidi = result.getAssignedMidiNote();
                applyFourierInstrument(
                        inst,
                        result.getSampleData(),
                        model,
                        result.getSummaryText() + " [Fourier Vocoder]",
                        true
                );
            });
            task.setOnFailed(e -> {
                if (this.samplerStatusLabel != null) {
                    this.samplerStatusLabel.setText("Erreur Phase Vocoder : " + task.getException().getMessage());
                }
            });
            Thread t = new Thread(task, "Wizard-Fourier-Builder");
            t.setDaemon(true);
            t.start();
            return;
        }

        this.sampler.setAdsr(result.getAttack(), result.getDecay(), result.getSustain(), result.getRelease());
        this.sampler.setMasterVolume((float) ((this.volume != null ? this.volume.getValue() : 100.0D) / 100.0D));

        if (result.isChromaticRootMode()) {
            this.sampler.setChromaticMode(true);
            this.sampler.setRootSample(result.getSampleData(), result.getLoopRegion());
            this.rootSampleFileName = result.getWavFile().getName();
            this.rootSampleMidi = result.getAssignedMidiNote();
        } else {
            this.sampler.assignNoteSample(
                    result.getAssignedMidiNote(),
                    result.getSampleData(),
                    result.getLoopRegion()
            );
            if (this.sampler.getRootSample() == null) {
                this.sampler.setChromaticMode(false);
            }
            this.assignedNoteFiles.put(result.getAssignedMidiNote(), result.getWavFile().getName());
        }

        ensureSamplerAudioRunning();
        activateSamplerMode(result.getSampleData(), null, result.getSummaryText(), false);
    }

    /**
     * Installe un SamplerInstrument généré par l'Empty3 Fourier Engine
     * et permet de le jouer directement avec le clavier de l'Accordéon 1.
     */
    public void applyFourierInstrument(
            SamplerInstrument instrument,
            SampleData previewSample,
            SpectralModel spectrumModel,
            String summaryText,
            boolean focusAccordion1
    ) {
        if (instrument == null) return;
        if (this.samplerAudioOutput != null && this.samplerAudioOutput.isRunning()) {
            this.samplerAudioOutput.stop();
        }
        this.sampler = new Sampler(instrument, 44100);
        this.sampler.setMasterVolume((float) ((this.volume != null ? this.volume.getValue() : 100.0D) / 100.0D));
        this.samplerAudioOutput = new DesktopAudioOutput(this.sampler, 44100, 256);
        this.samplerAudioOutput.setPcmListener((data, len) ->
                this.getSoundProductionSystem().writeWaveBuffer(data, len));
        this.samplerAudioOutput.start();

        if (previewSample != null) {
            this.rootSampleMidi = previewSample.getRootMidi();
            this.rootSampleFileName = instrument.getName();
        }

        activateSamplerMode(previewSample, spectrumModel, summaryText, focusAccordion1);
    }

    /**
     * Installe un SampleData transformé par Fourier en mode root/chromatique
     * et retourne vers le clavier de l'Accordéon 1 si demandé.
     */
    public void applyTransformedRootSample(
            SampleData sampleData,
            SpectralModel spectrumModel,
            String summaryText,
            boolean focusAccordion1
    ) {
        if (sampleData == null) return;
        this.sampler.setChromaticMode(true);
        this.sampler.setRootSample(sampleData, this.sampler.getLoopRegion());
        this.rootSampleMidi = sampleData.getRootMidi();
        this.rootSampleFileName = summaryText;
        ensureSamplerAudioRunning();
        activateSamplerMode(sampleData, spectrumModel, summaryText, focusAccordion1);
    }

    private void ensureSamplerAudioRunning() {
        if (this.samplerAudioOutput != null && !this.samplerAudioOutput.isRunning()) {
            try {
                this.samplerAudioOutput.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void activateSamplerMode(
            SampleData previewSample,
            SpectralModel spectrumModel,
            String summaryText,
            boolean focusAccordion1
    ) {
        if (this.samplerRadioButton != null) {
            this.samplerRadioButton.setSelected(true);
        }
        this.setForm(WaveForm.SAMPLER);

        if (this.samplerStatusLabel != null) {
            this.samplerStatusLabel.setText("Sampler: " + summaryText);
        }

        this.lastVisualSample = previewSample;
        this.lastVisualSpectrum = spectrumModel;
        this.lastVisualLabel = summaryText;
        refreshMainCanvas();
        this.updateKeyboardAssignmentBadges();

        if (focusAccordion1 && this.accordion != null && this.synthTitledPane != null) {
            this.accordion.setExpandedPane(this.synthTitledPane);
            this.synthTitledPane.requestFocus();
        }
    }

    private void refreshMainCanvas() {
        if (this.waveformCanvas == null) return;
        boolean showSpec = this.showSpectrumCheck != null && this.showSpectrumCheck.isSelected();
        if (showSpec) {
            if (this.lastVisualSpectrum == null && this.lastVisualSample != null) {
                float[] mono = FourierPanel.extractMono(this.lastVisualSample);
                int win = mono.length >= 2048 ? 2048 : 512;
                this.lastVisualSpectrum = SignalAnalyzer.analyze(
                        mono, this.lastVisualSample.getSampleRate(), win, win / 4
                );
            }
            FourierPanel.drawSpectrum(this.waveformCanvas, this.lastVisualSpectrum, this.lastVisualLabel);
        } else {
            SamplerWizardDialog.drawWaveform(
                    this.waveformCanvas,
                    this.lastVisualSample,
                    this.sampler != null ? this.sampler.getLoopRegion() : null,
                    this.lastVisualLabel != null
                            ? this.lastVisualLabel
                            : "Click 'WAV SAMPLER WIZARD...' to choose a WAV file & assign a note"
            );
        }
    }

    public void clearSamplerAssignments() {
        if (this.sampler != null) {
            this.sampler.clearAllSamples();
        }
        this.assignedNoteFiles.clear();
        this.rootSampleFileName = null;
        this.rootSampleMidi = -1;
        this.lastVisualSample = null;
        this.lastVisualSpectrum = null;
        this.lastVisualLabel = null;
        if (this.fourierPanel != null) {
            this.fourierPanel.setBaseSample(null, null);
        }
        if (this.samplerStatusLabel != null) {
            this.samplerStatusLabel.setText("Sampler: No WAV loaded");
        }
        refreshMainCanvas();
        this.updateKeyboardAssignmentBadges();
    }

    private String getDefaultBlackKeyStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #333333, #1a1a1a); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 16; -fx-cursor: hand;";
    }

    private String getDefaultWhiteKeyStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #ffffff, #e0e0e0); -fx-text-fill: #1a1a1a; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 14 20; -fx-border-color: #bbbbbb; -fx-cursor: hand;";
    }

    private void updateButtonNoteNumbers() {
        if (this.buttons == null) return;
        for (int i = 0; i < this.buttons.length; i++) {
            this.buttonNo[i] = this.getTone(this.buttons[i]);
        }
    }

    private void updateKeyboardAssignmentBadges() {
        if (this.buttons == null || this.baseButtonTexts == null) return;
        for (int i = 0; i < this.buttons.length; i++) {
            Button btn = this.buttons[i];
            String baseText = this.baseButtonTexts[i];
            if ("--".equals(baseText)) continue;

            int midi = getMidiNote(btn);
            boolean hasDirectSample = this.assignedNoteFiles.containsKey(midi);
            boolean isRootNote = (this.rootSampleMidi == midi && this.rootSampleFileName != null);

            if (hasDirectSample) {
                String fileName = this.assignedNoteFiles.get(midi);
                btn.setText(baseText + " [WAV]");
                btn.setTooltip(new Tooltip("Assigned WAV: " + fileName + " (" + SoundProductionSystem.midiToNoteName(midi) + ")"));
                btn.setStyle("-fx-background-color: linear-gradient(to bottom, #2da44e, #1a7f37); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 12 16; -fx-cursor: hand;");
            } else if (isRootNote) {
                btn.setText(baseText + " [ROOT]");
                btn.setTooltip(new Tooltip("Root WAV: " + this.rootSampleFileName + " (" + SoundProductionSystem.midiToNoteName(midi) + ")"));
                btn.setStyle("-fx-background-color: linear-gradient(to bottom, #0969da, #0550ae); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 12 16; -fx-cursor: hand;");
            } else {
                btn.setText(baseText);
                btn.setTooltip(new Tooltip(SoundProductionSystem.midiToNoteName(midi) + " (MIDI " + midi + ")"));
                btn.setStyle(i < 8 ? getDefaultBlackKeyStyle() : getDefaultWhiteKeyStyle());
            }
        }
    }

    private int getButtonIndex(Object source) {
        if (this.buttons == null) return -1;
        for (int i = 0; i < this.buttons.length; i++) {
            if (this.buttons[i] == source) {
                return i;
            }
        }
        return -1;
    }

    public int getMidiNote(Button source) {
        return SoundProductionSystem.equivToMidi(getTone(source));
    }

    public int getTone(Object source) {
        int btnIdx = getButtonIndex(source);
        String id = (btnIdx >= 0 && this.baseButtonTexts != null)
                ? this.baseButtonTexts[btnIdx]
                : ((Button) source).getText();
        if (id == null || id.length() < 2 || id.startsWith("--")) {
            return 0;
        }
        int index;
        String note;
        String diese = id.substring(0, 2);
        byte var6 = -1;
        switch (diese.hashCode()) {
            case 3211:
                if (diese.equals("do")) {
                    var6 = 0;
                }
                break;
            case 3259:
                if (diese.equals("fa")) {
                    var6 = 3;
                }
                break;
            case 3445:
                if (diese.equals("la")) {
                    var6 = 5;
                }
                break;
            case 3484:
                if (diese.equals("mi")) {
                    var6 = 2;
                }
                break;
            case 3635:
                if (diese.equals("re")) {
                    var6 = 1;
                }
                break;
            case 3670:
                if (diese.equals("si")) {
                    var6 = 6;
                }
                break;
            case 3676:
                if (diese.equals("so")) {
                    var6 = 4;
                }
        }

        switch (var6) {
            case 0:
                note = "C";
                index = 2;
                break;
            case 1:
                note = "D";
                index = 2;
                break;
            case 2:
                note = "E";
                index = 2;
                break;
            case 3:
                note = "F";
                index = 2;
                break;
            case 4:
                note = "G";
                index = 3;
                break;
            case 5:
                note = "A";
                index = 2;
                break;
            case 6:
                note = "B";
                index = 2;
                break;
            default:
                return 0;
        }

        diese = "";
        if (index < id.length() && id.charAt(index) == '#') {
            diese = "#";
        }

        int octave = (int) this.slider.getValue();
        if (btnIdx == 7 || btnIdx == 15) {
            octave += 1;
        }
        String noteAnglaise = note + diese + octave;
        return this.soundProductionSystem.equiv(noteAnglaise);
    }

    public void setForm(WaveForm userData) {
        if (userData != null && this.player != null) {
            this.player.setWaveform(userData);
        } else if (userData == null) {
            System.out.println("null waveform");
        }
    }

    public void playNote(Button source) {
        if (source == null || source.isDisabled() || this.player == null) return;
        if (this.activePressedButtons.contains(source)) {
            return;
        }
        this.activePressedButtons.add(source);

        int tone = this.getTone(source);
        int midiNote = Math.max(0, Math.min(127, SoundProductionSystem.equivToMidi(tone)));
        System.out.println("Tone: " + tone + " | MIDI: " + midiNote + " | Note: " + source.getText()
                + " | Octave: " + (int) this.slider.getValue() + " | Volume: " + this.player.getVolume());

        if (this.player.getForm() == WaveForm.SAMPLER && this.sampler != null && this.sampler.hasSampleForNote(midiNote)) {
            ensureSamplerAudioRunning();
            SampleData activeSample = this.sampler.getSampleForNote(midiNote);
            if (activeSample != null && this.waveformCanvas != null && (this.showSpectrumCheck == null || !this.showSpectrumCheck.isSelected())) {
                SamplerWizardDialog.drawWaveform(
                        this.waveformCanvas,
                        activeSample,
                        this.sampler.getLoopRegion(),
                        "Playing MIDI " + midiNote + " — " + SoundProductionSystem.midiToNoteName(midiNote)
                );
            }
        }

        Note note = (Note) this.noteMap.get(tone);
        if (note == null) {
            note = new Note(this.noteMap, this.minDurationSec, tone, this.player.getForm());
            this.noteMap.put(tone, note);
        }
        note.setWaveform(this.player.getForm());
        this.player.playNote(note);
    }

    public void stopNote(Button source) {
        if (source == null || this.player == null) return;
        if (!this.activePressedButtons.remove(source)) {
            return;
        }

        int tone = this.getTone(source);
        int midiNote = Math.max(0, Math.min(127, SoundProductionSystem.equivToMidi(tone)));
        if (this.sampler != null) {
            this.sampler.noteOff(midiNote);
        }
        Note note = (Note) this.noteMap.get(tone);
        if (note != null) {
            if (note.getTimer() != null) {
                System.out.println((double) note.getTimer().getTotalTimeElapsedNanoSec() / 1.0E9D);
            }
            this.player.stopNote(note);
            System.out.println("Key pressed: " + this.player.getCurrentNotes().size());
        }
    }

    public Button getButton(Integer halfTone) {
        for (int i = 0; i < this.buttons.length; ++i) {
            if ((halfTone).equals(this.buttonNo[i])) {
                return this.buttons[i];
            }
        }

        return null;
    }

    public Player getPlayer() {
        return this.player;
    }
}
