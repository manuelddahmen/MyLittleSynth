package one.empty3.apps.mylittlesynth;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import one.empty3.audio.sampler.core.DesktopAudioOutput;
import one.empty3.audio.sampler.core.LoopRegion;
import one.empty3.audio.sampler.core.SampleData;
import one.empty3.audio.sampler.core.Sampler;
import one.empty3.audio.sampler.core.WavReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public class SamplerWizardDialog {

    public static final String[] NOTE_LABELS = {
            "C (do)", "C# (do#)", "D (re)", "D# (re#)", "E (mi)", "F (fa)",
            "F# (fa#)", "G (sol)", "G# (sol#)", "A (la)", "A# (la#)", "B (si)"
    };

    public static final String[] ENGLISH_NOTES = {
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    public static final String[] SOLFEGE_NOTES = {
            "do", "do#", "re", "re#", "mi", "fa", "fa#", "sol", "sol#", "la", "la#", "si"
    };

    public static class WizardResult {
        private final File wavFile;
        private final SampleData sampleData;
        private final int assignedMidiNote;
        private final boolean chromaticRootMode;
        private final LoopRegion loopRegion;
        private final float attack;
        private final float decay;
        private final float sustain;
        private final float release;
        private final boolean usePhaseVocoder;
        private final boolean preserveFormants;

        public WizardResult(
                File wavFile,
                SampleData sampleData,
                int assignedMidiNote,
                boolean chromaticRootMode,
                LoopRegion loopRegion,
                float attack,
                float decay,
                float sustain,
                float release
        ) {
            this(wavFile, sampleData, assignedMidiNote, chromaticRootMode, loopRegion,
                    attack, decay, sustain, release, false, false);
        }

        public WizardResult(
                File wavFile,
                SampleData sampleData,
                int assignedMidiNote,
                boolean chromaticRootMode,
                LoopRegion loopRegion,
                float attack,
                float decay,
                float sustain,
                float release,
                boolean usePhaseVocoder,
                boolean preserveFormants
        ) {
            this.wavFile = wavFile;
            this.sampleData = sampleData;
            this.assignedMidiNote = assignedMidiNote;
            this.chromaticRootMode = chromaticRootMode;
            this.loopRegion = loopRegion;
            this.attack = attack;
            this.decay = decay;
            this.sustain = sustain;
            this.release = release;
            this.usePhaseVocoder = usePhaseVocoder;
            this.preserveFormants = preserveFormants;
        }

        public boolean isUsePhaseVocoder() {
            return usePhaseVocoder;
        }

        public boolean isPreserveFormants() {
            return preserveFormants;
        }

        public File getWavFile() {
            return wavFile;
        }

        public SampleData getSampleData() {
            return sampleData;
        }

        public int getAssignedMidiNote() {
            return assignedMidiNote;
        }

        public boolean isChromaticRootMode() {
            return chromaticRootMode;
        }

        public LoopRegion getLoopRegion() {
            return loopRegion;
        }

        public float getAttack() {
            return attack;
        }

        public float getDecay() {
            return decay;
        }

        public float getSustain() {
            return sustain;
        }

        public float getRelease() {
            return release;
        }

        public String getSummaryText() {
            String mode = chromaticRootMode ? "Root/Chromatic" : "Single Note";
            return wavFile.getName() + " -> " + SoundProductionSystem.midiToNoteName(assignedMidiNote)
                    + " [MIDI " + assignedMidiNote + ", " + mode + "]";
        }
    }

    public static int computeMidiNote(int semitoneIndex, int octave) {
        int clampedSemitone = Math.floorMod(semitoneIndex, 12);
        int midi = (octave + 1) * 12 + clampedSemitone;
        return Math.max(0, Math.min(127, midi));
    }

    public static SampleData loadWavFile(File file, int rootMidi) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return WavReader.read(fis, rootMidi);
        }
    }

    public static void drawWaveform(Canvas canvas, SampleData sampleData, LoopRegion loopRegion, String label) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.setFill(Color.web("#161b22"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#30363d"));
        gc.setLineWidth(1.0);
        gc.strokeLine(0, h / 2.0, w, h / 2.0);
        gc.strokeRect(0.5, 0.5, w - 1, h - 1);

        if (sampleData == null || sampleData.getFrameCount() == 0) {
            gc.setFill(Color.web("#8b949e"));
            gc.fillText(label != null ? label : "No WAV sample loaded", 16, h / 2.0 - 4);
            return;
        }

        int frameCount = sampleData.getFrameCount();
        if (loopRegion != null && loopRegion.getEnabled() && loopRegion.isValid(sampleData)) {
            double xStart = (loopRegion.getStartFrame() / (double) frameCount) * w;
            double xEnd = (loopRegion.getEndFrame() / (double) frameCount) * w;
            gc.setFill(Color.color(0.2, 0.8, 0.4, 0.18));
            gc.fillRect(xStart, 0, Math.max(2.0, xEnd - xStart), h);
            gc.setStroke(Color.web("#3fb950"));
            gc.strokeLine(xStart, 0, xStart, h);
            gc.strokeLine(xEnd, 0, xEnd, h);
        }

        gc.setStroke(Color.web("#58a6ff"));
        gc.setLineWidth(1.1);
        int cols = Math.max(1, (int) w);
        double mid = h / 2.0;
        double amp = (h / 2.0) - 10.0;

        for (int x = 0; x < cols; x++) {
            int startFrame = (int) ((x / (double) cols) * frameCount);
            int endFrame = Math.min(frameCount, Math.max(startFrame + 1, (int) (((x + 1) / (double) cols) * frameCount)));
            int step = Math.max(1, (endFrame - startFrame) / 16);
            float minVal = 0f;
            float maxVal = 0f;
            for (int f = startFrame; f < endFrame; f += step) {
                float s = sampleData.getSample(f, 0);
                if (s < minVal) minVal = s;
                if (s > maxVal) maxVal = s;
            }
            double y1 = mid - maxVal * amp;
            double y2 = mid - minVal * amp;
            gc.strokeLine(x, y1, x, Math.max(y1 + 1.0, y2));
        }

        gc.setFill(Color.web("#f0f6fc"));
        String header = (label != null ? label : "Sample")
                + String.format(Locale.US, " (%d Hz, %d ch, %.2f s)",
                sampleData.getSampleRate(),
                sampleData.getChannels(),
                frameCount / (double) sampleData.getSampleRate());
        gc.fillText(header, 10, 18);
    }

    private File selectedFile;
    private byte[] rawWavBytes;
    private SampleData loadedSample;
    private int selectedSemitone = 0; // C (do)
    private int selectedOctave = 4;
    private boolean chromaticRootMode = true;
    private boolean loopEnabled = false;
    private int loopStartFrame = 0;
    private int loopEndFrame = 0;
    private float attack = 0.01f;
    private float decay = 0.15f;
    private float sustain = 0.75f;
    private float release = 0.25f;
    private boolean usePhaseVocoder = false;
    private boolean preserveFormants = false;

    private int currentStep = 0;
    private WizardResult wizardResult = null;

    public Optional<WizardResult> showAndWait(Window owner, int initialOctave) {
        this.selectedOctave = Math.max(0, Math.min(8, initialOctave));

        Stage dialog = new Stage();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("WAV Sampler Setup Wizard");

        Label stepTitleLabel = new Label();
        stepTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        ProgressBar stepProgress = new ProgressBar(1.0 / 3.0);
        stepProgress.setMaxWidth(Double.MAX_VALUE);

        VBox headerBox = new VBox(6, stepTitleLabel, stepProgress);
        headerBox.setPadding(new Insets(12, 16, 10, 16));
        headerBox.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #d0d7de; -fx-border-width: 0 0 1 0;");

        Canvas waveformCanvas = new Canvas(520, 130);
        drawWaveform(waveformCanvas, null, null, "Choose a WAV file to preview its waveform");

        // --- STEP 1: Choose WAV file ---
        Label filePathLabel = new Label("No WAV file selected");
        filePathLabel.setStyle("-fx-font-weight: bold;");
        Label fileMetaLabel = new Label("Select a .wav file (8/16/24/32-bit PCM or 32-bit float, mono or stereo).");
        fileMetaLabel.setWrapText(true);

        Button browseButton = new Button("Browse WAV File...");
        browseButton.setStyle("-fx-font-weight: bold; -fx-padding: 8 16;");

        ComboBox<File> workspaceWavCombo = new ComboBox<>();
        workspaceWavCombo.setPromptText("Or pick a WAV file from project folder...");
        File cwd = new File(".");
        File[] localWavs = cwd.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".wav"));
        if (localWavs != null && localWavs.length > 0) {
            Arrays.sort(localWavs);
            workspaceWavCombo.getItems().addAll(localWavs);
        } else {
            workspaceWavCombo.setDisable(true);
        }

        // Loop controls needed for updating when file loads
        CheckBox loopCheck = new CheckBox("Enable sustain loop while key is held");
        Slider loopStartSlider = new Slider(0, 100, 0);
        Slider loopEndSlider = new Slider(0, 100, 100);
        Label loopInfoLabel = new Label("Loop frames: disabled");

        Runnable refreshPreview = () -> {
            LoopRegion lr = buildCurrentLoopRegion();
            String label = selectedFile != null
                    ? selectedFile.getName() + " @ " + SoundProductionSystem.midiToNoteName(getSelectedMidiNote())
                    : "Choose a WAV file";
            drawWaveform(waveformCanvas, loadedSample, lr, label);
        };

        java.util.function.Consumer<File> loadFileAction = (File file) -> {
            if (file == null || !file.isFile()) return;
            try {
                byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
                int midi = getSelectedMidiNote();
                SampleData data = WavReader.decode(bytes, midi);
                this.selectedFile = file;
                this.rawWavBytes = bytes;
                this.loadedSample = data;
                this.loopStartFrame = 0;
                this.loopEndFrame = data.getFrameCount();
                loopStartSlider.setMax(Math.max(1, data.getFrameCount() - 2));
                loopEndSlider.setMax(Math.max(2, data.getFrameCount()));
                loopStartSlider.setValue(0);
                loopEndSlider.setValue(data.getFrameCount());

                double durationSec = data.getFrameCount() / (double) data.getSampleRate();
                filePathLabel.setText("Loaded: " + file.getName());
                filePathLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1a7f37;");
                fileMetaLabel.setText(String.format(Locale.US,
                        "Path: %s\nSample Rate: %d Hz | Channels: %d (%s) | Frames: %d | Duration: %.2f s",
                        file.getAbsolutePath(),
                        data.getSampleRate(),
                        data.getChannels(),
                        data.getChannels() == 1 ? "Mono" : "Stereo",
                        data.getFrameCount(),
                        durationSec));
                refreshPreview.run();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Could not read WAV file:\n" + ex.getMessage(),
                        ButtonType.OK);
                alert.initOwner(dialog);
                alert.showAndWait();
            }
        };

        browseButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select WAV Audio Sample");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("WAV Audio Files (*.wav)", "*.wav", "*.WAV")
            );
            if (selectedFile != null && selectedFile.getParentFile() != null && selectedFile.getParentFile().isDirectory()) {
                chooser.setInitialDirectory(selectedFile.getParentFile());
            } else if (cwd.isDirectory()) {
                chooser.setInitialDirectory(cwd.getAbsoluteFile());
            }
            File chosen = chooser.showOpenDialog(dialog);
            if (chosen != null) {
                loadFileAction.accept(chosen);
            }
        });

        workspaceWavCombo.setOnAction(e -> {
            File chosen = workspaceWavCombo.getValue();
            if (chosen != null) {
                loadFileAction.accept(chosen);
            }
        });

        HBox fileButtonsRow = new HBox(10, browseButton, workspaceWavCombo);
        fileButtonsRow.setAlignment(Pos.CENTER_LEFT);

        VBox step1Pane = new VBox(12,
                new Label("1. Select a WAV sample file from your computer:"),
                fileButtonsRow,
                filePathLabel,
                fileMetaLabel
        );
        step1Pane.setPadding(new Insets(10));

        // --- STEP 2: Assign Note & Mode ---
        ComboBox<String> noteCombo = new ComboBox<>();
        noteCombo.getItems().addAll(NOTE_LABELS);
        noteCombo.getSelectionModel().select(selectedSemitone);

        Spinner<Integer> octaveSpinner = new Spinner<>(0, 8, selectedOctave);
        octaveSpinner.setPrefWidth(80);

        Label assignedNoteSummary = new Label();
        assignedNoteSummary.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0969da;");

        Button[] miniKeys = new Button[12];
        HBox blackKeyRow = new HBox(6);
        HBox whiteKeyRow = new HBox(6);

        Runnable updateNoteSelectionUI = () -> {
            int midi = getSelectedMidiNote();
            if (rawWavBytes != null) {
                loadedSample = WavReader.decode(rawWavBytes, midi);
            }
            int equivTone = SoundProductionSystem.midiToEquiv(midi);
            float freq = SoundProductionSystem.getFrequency(equivTone - 44);
            assignedNoteSummary.setText(String.format(Locale.US,
                    "Assigned Note: %s  |  MIDI Note: %d  |  Frequency: %.2f Hz",
                    SoundProductionSystem.midiToNoteName(midi), midi, freq));
            for (int k = 0; k < 12; k++) {
                boolean isSelected = (k == selectedSemitone);
                boolean isSharp = ENGLISH_NOTES[k].contains("#");
                if (isSelected) {
                    miniKeys[k].setStyle("-fx-background-color: #0969da; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 10;");
                } else if (isSharp) {
                    miniKeys[k].setStyle("-fx-background-color: #24292f; -fx-text-fill: white; -fx-padding: 8 10;");
                } else {
                    miniKeys[k].setStyle("-fx-background-color: #ffffff; -fx-text-fill: #24292f; -fx-border-color: #afb8c1; -fx-padding: 10 12;");
                }
            }
            refreshPreview.run();
        };

        for (int k = 0; k < 12; k++) {
            final int semitoneIdx = k;
            Button keyBtn = new Button(SOLFEGE_NOTES[k] + "\n(" + ENGLISH_NOTES[k] + ")");
            miniKeys[k] = keyBtn;
            keyBtn.setOnAction(e -> {
                selectedSemitone = semitoneIdx;
                noteCombo.getSelectionModel().select(semitoneIdx);
                updateNoteSelectionUI.run();
            });
            if (ENGLISH_NOTES[k].contains("#")) {
                blackKeyRow.getChildren().add(keyBtn);
            } else {
                whiteKeyRow.getChildren().add(keyBtn);
            }
        }

        noteCombo.setOnAction(e -> {
            int idx = noteCombo.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                selectedSemitone = idx;
                updateNoteSelectionUI.run();
            }
        });

        octaveSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                selectedOctave = newV;
                updateNoteSelectionUI.run();
            }
        });

        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton chromaticRb = new RadioButton(
                "Root Note / Chromatic Mode — Transpose this WAV across all keyboard notes relative to the assigned note"
        );
        chromaticRb.setToggleGroup(modeGroup);
        chromaticRb.setSelected(true);

        RadioButton singleNoteRb = new RadioButton(
                "Single Note Mode — Assign this WAV exclusively to the selected note (can assign different WAVs per note)"
        );
        singleNoteRb.setToggleGroup(modeGroup);

        chromaticRb.setOnAction(e -> chromaticRootMode = true);
        singleNoteRb.setOnAction(e -> chromaticRootMode = false);

        HBox noteControlsRow = new HBox(12,
                new Label("Note:"), noteCombo,
                new Label("Octave:"), octaveSpinner
        );
        noteControlsRow.setAlignment(Pos.CENTER_LEFT);

        VBox miniPianoBox = new VBox(6,
                new Label("Or click a key on the mini keyboard to pick the note:"),
                blackKeyRow,
                whiteKeyRow
        );
        miniPianoBox.setPadding(new Insets(6, 0, 6, 0));

        VBox step2Pane = new VBox(10,
                new Label("2. Choose the note to assign to this WAV sample:"),
                noteControlsRow,
                miniPianoBox,
                assignedNoteSummary,
                new Separator(),
                new Label("Mapping Mode:"),
                chromaticRb,
                singleNoteRb
        );
        step2Pane.setPadding(new Insets(10));

        // --- STEP 3: Loop, ADSR & Audition Preview ---
        Runnable updateLoopInfo = () -> {
            loopEnabled = loopCheck.isSelected();
            loopStartFrame = (int) loopStartSlider.getValue();
            loopEndFrame = (int) loopEndSlider.getValue();
            if (loopEndFrame <= loopStartFrame + 2 && loadedSample != null) {
                loopEndFrame = Math.min(loadedSample.getFrameCount(), loopStartFrame + 2);
            }
            if (loopEnabled) {
                loopInfoLabel.setText("Loop active: frame " + loopStartFrame + " -> " + loopEndFrame);
            } else {
                loopInfoLabel.setText("Loop frames: disabled (one-shot playback)");
            }
            refreshPreview.run();
        };

        loopCheck.setOnAction(e -> updateLoopInfo.run());
        loopStartSlider.valueProperty().addListener((obs, o, n) -> updateLoopInfo.run());
        loopEndSlider.valueProperty().addListener((obs, o, n) -> updateLoopInfo.run());

        Slider attackSlider = new Slider(0.001, 2.0, attack);
        Slider decaySlider = new Slider(0.01, 2.0, decay);
        Slider sustainSlider = new Slider(0.0, 1.0, sustain);
        Slider releaseSlider = new Slider(0.01, 3.0, release);

        attackSlider.valueProperty().addListener((obs, o, n) -> attack = n.floatValue());
        decaySlider.valueProperty().addListener((obs, o, n) -> decay = n.floatValue());
        sustainSlider.valueProperty().addListener((obs, o, n) -> sustain = n.floatValue());
        releaseSlider.valueProperty().addListener((obs, o, n) -> release = n.floatValue());

        GridPane adsrGrid = new GridPane();
        adsrGrid.setHgap(10);
        adsrGrid.setVgap(6);
        adsrGrid.addRow(0, new Label("Loop Start:"), loopStartSlider, new Label("Loop End:"), loopEndSlider);
        adsrGrid.addRow(1, new Label("Attack (s):"), attackSlider, new Label("Decay (s):"), decaySlider);
        adsrGrid.addRow(2, new Label("Sustain:"), sustainSlider, new Label("Release (s):"), releaseSlider);

        Button previewBtn = new Button("▶ Hold to Test Assigned Note");
        previewBtn.setStyle("-fx-background-color: #2da44e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 18;");

        final Sampler[] previewSamplerRef = new Sampler[1];
        final DesktopAudioOutput[] previewOutputRef = new DesktopAudioOutput[1];

        Runnable stopPreviewAudio = () -> {
            if (previewSamplerRef[0] != null) {
                previewSamplerRef[0].allNotesOff();
            }
            if (previewOutputRef[0] != null) {
                previewOutputRef[0].stop();
                previewOutputRef[0] = null;
            }
        };

        previewBtn.setOnMousePressed(e -> {
            if (loadedSample == null) return;
            stopPreviewAudio.run();
            Sampler s = new Sampler(loadedSample, 44100, 8);
            s.setLoopRegion(buildCurrentLoopRegion());
            s.setAdsr(attack, decay, sustain, release);
            DesktopAudioOutput out = new DesktopAudioOutput(s, 44100, 256);
            out.start();
            previewSamplerRef[0] = s;
            previewOutputRef[0] = out;
            s.noteOn(getSelectedMidiNote(), 1.0f);
        });

        previewBtn.setOnMouseReleased(e -> {
            if (previewSamplerRef[0] != null) {
                previewSamplerRef[0].noteOff(getSelectedMidiNote());
            }
        });

        CheckBox vocoderCheck = new CheckBox("Phase Vocoder (Fourier Engine — transposition à durée constante)");
        CheckBox formantsCheck = new CheckBox("Préserver les formants");
        formantsCheck.setDisable(true);
        vocoderCheck.setOnAction(e -> {
            usePhaseVocoder = vocoderCheck.isSelected();
            formantsCheck.setDisable(!usePhaseVocoder);
        });
        formantsCheck.setOnAction(e -> preserveFormants = formantsCheck.isSelected());

        VBox step3Pane = new VBox(10,
                new Label("3. Configure sustain loop, ADSR envelope, Fourier Phase Vocoder & preview:"),
                loopCheck,
                loopInfoLabel,
                adsrGrid,
                new HBox(12, vocoderCheck, formantsCheck),
                previewBtn
        );
        step3Pane.setPadding(new Insets(10));

        StackPane stepContainer = new StackPane(step1Pane, step2Pane, step3Pane);

        // --- Navigation Footer ---
        Button backBtn = new Button("< Back");
        Button nextBtn = new Button("Next >");
        Button finishBtn = new Button("Finish & Apply");
        finishBtn.setStyle("-fx-background-color: #0969da; -fx-text-fill: white; -fx-font-weight: bold;");
        Button cancelBtn = new Button("Cancel");

        Runnable updateStepView = () -> {
            step1Pane.setVisible(currentStep == 0);
            step1Pane.setManaged(currentStep == 0);
            step2Pane.setVisible(currentStep == 1);
            step2Pane.setManaged(currentStep == 1);
            step3Pane.setVisible(currentStep == 2);
            step3Pane.setManaged(currentStep == 2);

            backBtn.setDisable(currentStep == 0);
            nextBtn.setDisable(currentStep == 2);
            stepProgress.setProgress((currentStep + 1) / 3.0);

            if (currentStep == 0) {
                stepTitleLabel.setText("Step 1 of 3 — Choose WAV Sample File");
            } else if (currentStep == 1) {
                stepTitleLabel.setText("Step 2 of 3 — Assign Note & Mapping Mode");
            } else {
                stepTitleLabel.setText("Step 3 of 3 — Loop, Envelope & Preview");
            }
        };

        backBtn.setOnAction(e -> {
            if (currentStep > 0) {
                currentStep--;
                updateStepView.run();
            }
        });

        nextBtn.setOnAction(e -> {
            if (currentStep == 0 && loadedSample == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        "Please select a WAV file first before proceeding to note assignment.",
                        ButtonType.OK);
                alert.initOwner(dialog);
                alert.showAndWait();
                return;
            }
            if (currentStep < 2) {
                currentStep++;
                updateStepView.run();
            }
        });

        finishBtn.setOnAction(e -> {
            if (loadedSample == null || selectedFile == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        "Please select a WAV file before finishing the wizard.",
                        ButtonType.OK);
                alert.initOwner(dialog);
                alert.showAndWait();
                return;
            }
            stopPreviewAudio.run();
            int finalMidi = getSelectedMidiNote();
            SampleData finalSample = WavReader.decode(rawWavBytes, finalMidi);
            wizardResult = new WizardResult(
                    selectedFile,
                    finalSample,
                    finalMidi,
                    chromaticRootMode,
                    buildCurrentLoopRegion(),
                    attack,
                    decay,
                    sustain,
                    release,
                    usePhaseVocoder,
                    preserveFormants
            );
            dialog.close();
        });

        cancelBtn.setOnAction(e -> {
            stopPreviewAudio.run();
            wizardResult = null;
            dialog.close();
        });

        dialog.setOnHidden(e -> stopPreviewAudio.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footerBox = new HBox(10, cancelBtn, spacer, backBtn, nextBtn, finishBtn);
        footerBox.setPadding(new Insets(12, 16, 12, 16));
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        footerBox.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #d0d7de; -fx-border-width: 1 0 0 0;");

        VBox centerContent = new VBox(10, stepContainer, waveformCanvas);
        centerContent.setPadding(new Insets(10, 16, 10, 16));

        BorderPane root = new BorderPane();
        root.setTop(headerBox);
        root.setCenter(centerContent);
        root.setBottom(footerBox);

        updateNoteSelectionUI.run();
        updateStepView.run();

        Scene scene = new Scene(root, 560, 560);
        dialog.setScene(scene);
        dialog.showAndWait();

        return Optional.ofNullable(wizardResult);
    }

    private int getSelectedMidiNote() {
        return computeMidiNote(selectedSemitone, selectedOctave);
    }

    private LoopRegion buildCurrentLoopRegion() {
        if (!loopEnabled || loadedSample == null) {
            return null;
        }
        int start = Math.max(0, Math.min(loopStartFrame, loadedSample.getFrameCount() - 2));
        int end = Math.max(start + 2, Math.min(loopEndFrame, loadedSample.getFrameCount()));
        LoopRegion region = new LoopRegion(start, end, true);
        return region.isValid(loadedSample) ? region : null;
    }
}
