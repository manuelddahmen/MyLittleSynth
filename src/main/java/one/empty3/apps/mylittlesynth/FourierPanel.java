package one.empty3.apps.mylittlesynth;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import one.empty3.audio.sampler.core.AdsrSettings;
import one.empty3.audio.sampler.core.LoopRegion;
import one.empty3.audio.sampler.core.SampleData;
import one.empty3.audio.sampler.core.SamplerInstrument;
import one.empty3.audio.sampler.core.WavReader;
import one.empty3.audio.sampler.dsp.FourierEngine;
import one.empty3.audio.sampler.dsp.analyzer.SignalAnalyzer;
import one.empty3.audio.sampler.dsp.model.SinusoidalComponent;
import one.empty3.audio.sampler.dsp.model.SpectralModel;
import one.empty3.audio.sampler.dsp.model.SpectralModelFrame;
import one.empty3.audio.sampler.dsp.stft.STFT;
import one.empty3.audio.sampler.dsp.stft.STFTAnalysis;
import one.empty3.audio.sampler.dsp.synth.FourierSynthesizer;
import one.empty3.audio.sampler.dsp.transform.TimbreTransform;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Panneau UX pour l'Empty3 Fourier Engine :
 * - Analyse spectrale (FFT / STFT / pics sinusoïdaux)
 * - Transformation spectrale (pente, formants, filtre passe-bande, morphing)
 * - Phase Vocoder (time-stretch et transposition à durée constante)
 * - Génération d'un SamplerInstrument jouable directement au clavier de l'Accordéon 1.
 */
public class FourierPanel extends VBox {

    private final App app;
    private SampleData baseSample;
    private String baseSampleName;
    private SampleData morphSample;
    private String morphSampleName;

    private final Label sourceLabel = new Label("Source : aucun WAV chargé (utilisez le Wizard ou 'Charger WAV...')");
    private final Label morphLabel = new Label("Morph 2e WAV : aucun");
    private final Label statusLabel = new Label("Prêt.");
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final Canvas spectrumCanvas = new Canvas(600, 150);

    private final CheckBox phaseVocoderCheck = new CheckBox("Phase Vocoder (transposition à durée constante)");
    private final CheckBox preserveFormantsCheck = new CheckBox("Préserver les formants");
    private final Slider tiltSlider = new Slider(-12.0, 12.0, 0.0);
    private final Slider formantSlider = new Slider(0.5, 2.0, 1.0);
    private final Slider stretchSlider = new Slider(0.5, 2.0, 1.0);
    private final Slider morphSlider = new Slider(0.0, 1.0, 0.0);
    private final Slider lowCutSlider = new Slider(0.0, 2000.0, 0.0);
    private final Slider highCutSlider = new Slider(1000.0, 20000.0, 20000.0);

    private final Button applyBtn = new Button("Appliquer & Jouer dans l'Accordéon 1");

    public FourierPanel(App app) {
        this.app = app;
        setSpacing(10);
        setPadding(new Insets(12));

        phaseVocoderCheck.setSelected(true);
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(22, 22);

        Button loadPrimaryBtn = new Button("Charger WAV source...");
        loadPrimaryBtn.setOnAction(e -> chooseWavFile(true));

        Button loadMorphBtn = new Button("Charger 2e WAV (Morph)...");
        loadMorphBtn.setOnAction(e -> chooseWavFile(false));

        Button clearMorphBtn = new Button("Effacer Morph");
        clearMorphBtn.setOnAction(e -> {
            this.morphSample = null;
            this.morphSampleName = null;
            this.morphSlider.setValue(0.0);
            this.morphLabel.setText("Morph 2e WAV : aucun");
        });

        HBox fileRow = new HBox(10, loadPrimaryBtn, loadMorphBtn, clearMorphBtn);
        fileRow.setAlignment(Pos.CENTER_LEFT);

        Label tiltVal = new Label("0.0 dB/oct");
        tiltSlider.valueProperty().addListener((o, ov, nv) ->
                tiltVal.setText(String.format(Locale.US, "%+.1f dB/oct", nv.doubleValue())));

        Label formantVal = new Label("1.00x");
        formantSlider.valueProperty().addListener((o, ov, nv) ->
                formantVal.setText(String.format(Locale.US, "%.2fx", nv.doubleValue())));

        Label stretchVal = new Label("1.00x");
        stretchSlider.valueProperty().addListener((o, ov, nv) ->
                stretchVal.setText(String.format(Locale.US, "%.2fx", nv.doubleValue())));

        Label morphVal = new Label("0%");
        morphSlider.valueProperty().addListener((o, ov, nv) ->
                morphVal.setText(String.format(Locale.US, "%.0f%%", nv.doubleValue() * 100.0)));

        Label lowCutVal = new Label("0 Hz");
        lowCutSlider.valueProperty().addListener((o, ov, nv) ->
                lowCutVal.setText(String.format(Locale.US, "%.0f Hz", nv.doubleValue())));

        Label highCutVal = new Label("20000 Hz");
        highCutSlider.valueProperty().addListener((o, ov, nv) ->
                highCutVal.setText(String.format(Locale.US, "%.0f Hz", nv.doubleValue())));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.addRow(0, new Label("Pente spectrale :"), tiltSlider, tiltVal,
                new Label("Formant Shift :"), formantSlider, formantVal);
        grid.addRow(1, new Label("Time-Stretch :"), stretchSlider, stretchVal,
                new Label("Morphing :"), morphSlider, morphVal);
        grid.addRow(2, new Label("Coupe-bas :"), lowCutSlider, lowCutVal,
                new Label("Coupe-haut :"), highCutSlider, highCutVal);

        HBox optionsRow = new HBox(16, phaseVocoderCheck, preserveFormantsCheck);
        optionsRow.setAlignment(Pos.CENTER_LEFT);

        applyBtn.setStyle("-fx-background-color: #0969da; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        applyBtn.setOnAction(e -> applyFourierProcessing());

        Button resetBtn = new Button("Réinitialiser paramètres");
        resetBtn.setOnAction(e -> {
            tiltSlider.setValue(0.0);
            formantSlider.setValue(1.0);
            stretchSlider.setValue(1.0);
            morphSlider.setValue(0.0);
            lowCutSlider.setValue(0.0);
            highCutSlider.setValue(20000.0);
            phaseVocoderCheck.setSelected(true);
            preserveFormantsCheck.setSelected(false);
        });

        HBox actionRow = new HBox(10, applyBtn, resetBtn, progressIndicator, statusLabel);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        drawSpectrum(spectrumCanvas, null, "Spectre FFT — Chargez un WAV pour analyser les composantes");

        getChildren().addAll(
                sourceLabel,
                morphLabel,
                fileRow,
                optionsRow,
                grid,
                actionRow,
                spectrumCanvas
        );
    }

    public void setBaseSample(SampleData sample, String fileName) {
        this.baseSample = sample;
        this.baseSampleName = fileName;
        if (sample == null) {
            sourceLabel.setText("Source : aucun WAV chargé (utilisez le Wizard ou 'Charger WAV...')");
            drawSpectrum(spectrumCanvas, null, "Spectre FFT — Aucun WAV chargé");
            return;
        }
        sourceLabel.setText(String.format(Locale.US,
                "Source : %s (%d Hz, %d ch, %.2f s, root MIDI %d — %s)",
                fileName != null ? fileName : "Sample",
                sample.getSampleRate(),
                sample.getChannels(),
                sample.getFrameCount() / (double) sample.getSampleRate(),
                sample.getRootMidi(),
                SoundProductionSystem.midiToNoteName(sample.getRootMidi())));
        analyzeAndDrawSpectrumAsync(sample, fileName);
    }

    private void chooseWavFile(boolean primary) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(primary ? "Choisir un WAV source" : "Choisir un 2e WAV pour le morphing");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("WAV Audio (*.wav)", "*.wav", "*.WAV")
        );
        File cwd = new File(".");
        if (cwd.isDirectory()) {
            chooser.setInitialDirectory(cwd.getAbsoluteFile());
        }
        File file = chooser.showOpenDialog(getScene() != null ? getScene().getWindow() : null);
        if (file == null) return;

        try (FileInputStream fis = new FileInputStream(file)) {
            int rootMidi = baseSample != null ? baseSample.getRootMidi() : 60;
            SampleData loaded = WavReader.read(fis, rootMidi);
            if (primary) {
                setBaseSample(loaded, file.getName());
            } else {
                this.morphSample = loaded;
                this.morphSampleName = file.getName();
                this.morphLabel.setText("Morph 2e WAV : " + file.getName());
                if (this.morphSlider.getValue() < 0.05) {
                    this.morphSlider.setValue(0.5);
                }
            }
        } catch (Exception ex) {
            statusLabel.setText("Erreur lecture WAV : " + ex.getMessage());
        }
    }

    private void analyzeAndDrawSpectrumAsync(SampleData sample, String label) {
        Task<SpectralModel> task = new Task<>() {
            @Override
            protected SpectralModel call() {
                float[] mono = extractMono(sample);
                int win = mono.length >= 2048 ? 2048 : 512;
                int hop = win / 4;
                return SignalAnalyzer.analyze(mono, sample.getSampleRate(), win, hop);
            }
        };
        task.setOnSucceeded(e -> drawSpectrum(spectrumCanvas, task.getValue(),
                "Spectre FFT : " + (label != null ? label : "Sample")));
        Thread t = new Thread(task, "Fourier-Spectrum-Analyzer");
        t.setDaemon(true);
        t.start();
    }

    private void applyFourierProcessing() {
        if (baseSample == null) {
            statusLabel.setText("Veuillez d'abord charger un fichier WAV.");
            return;
        }

        final SampleData source = baseSample;
        final SampleData morph = morphSample;
        final String name = baseSampleName != null ? baseSampleName : "FourierInstrument";
        final boolean useVocoder = phaseVocoderCheck.isSelected();
        final boolean preserveFormants = preserveFormantsCheck.isSelected();
        final double tiltDb = tiltSlider.getValue();
        final double formantRatio = formantSlider.getValue();
        final double stretch = stretchSlider.getValue();
        final double morphMix = morph != null ? morphSlider.getValue() : 0.0;
        final double lowCut = lowCutSlider.getValue();
        final double highCut = highCutSlider.getValue();

        applyBtn.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("Traitement Fourier en cours...");

        Task<FourierBuildOutput> task = new Task<>() {
            @Override
            protected FourierBuildOutput call() {
                SampleData transformedBase = processSample(
                        source, morph, tiltDb, formantRatio, stretch, morphMix, lowCut, highCut
                );

                List<Integer> anchors = buildAnchorMidiNotes(transformedBase.getRootMidi(), useVocoder);
                SamplerInstrument instrument;
                if (useVocoder) {
                    instrument = FourierEngine.buildInstrument(
                            name,
                            transformedBase,
                            anchors,
                            preserveFormants,
                            0.0,
                            1.0,
                            16,
                            new AdsrSettings(),
                            app.getSampler() != null ? app.getSampler().getLoopRegion() : null
                    );
                } else {
                    instrument = null;
                }

                float[] mono = extractMono(transformedBase);
                int win = mono.length >= 2048 ? 2048 : 512;
                SpectralModel model = SignalAnalyzer.analyze(mono, transformedBase.getSampleRate(), win, win / 4);
                return new FourierBuildOutput(transformedBase, instrument, model);
            }
        };

        task.setOnSucceeded(e -> {
            applyBtn.setDisable(false);
            progressIndicator.setVisible(false);
            FourierBuildOutput out = task.getValue();
            String summary = String.format(Locale.US,
                    "Fourier [%s] %s | Tilt %+.1f dB | Formant %.2fx | Stretch %.2fx",
                    name,
                    useVocoder ? "(Phase Vocoder)" : "(Resampling)",
                    tiltDb, formantRatio, stretch);
            statusLabel.setText("Prêt — " + summary);
            drawSpectrum(spectrumCanvas, out.model, summary);

            if (out.instrument != null) {
                app.applyFourierInstrument(out.instrument, out.sampleData, out.model, summary, true);
            } else {
                app.applyTransformedRootSample(out.sampleData, out.model, summary, true);
            }
        });

        task.setOnFailed(e -> {
            applyBtn.setDisable(false);
            progressIndicator.setVisible(false);
            Throwable ex = task.getException();
            statusLabel.setText("Erreur Fourier : " + (ex != null ? ex.getMessage() : "inconnue"));
            if (ex != null) ex.printStackTrace();
        });

        Thread worker = new Thread(task, "Empty3-Fourier-Worker");
        worker.setDaemon(true);
        worker.start();
    }

    public static List<Integer> buildAnchorMidiNotes(int rootMidi, boolean usePhaseVocoder) {
        List<Integer> notes = new ArrayList<>();
        if (!usePhaseVocoder) {
            notes.add(rootMidi);
            return notes;
        }
        int[] offsets = {-12, -6, 0, 6, 12};
        for (int off : offsets) {
            int midi = rootMidi + off;
            if (midi >= 0 && midi <= 127 && !notes.contains(midi)) {
                notes.add(midi);
            }
        }
        return notes;
    }

    private static SampleData processSample(
            SampleData source,
            SampleData morph,
            double tiltDb,
            double formantRatio,
            double stretch,
            double morphMix,
            double lowCut,
            double highCut
    ) {
        int channels = source.getChannels();
        int frames = source.getFrameCount();
        int sr = source.getSampleRate();
        int win = frames >= 2048 ? 2048 : 512;
        int hop = win / 4;

        float[][] processedChannels = new float[channels][];
        for (int ch = 0; ch < channels; ch++) {
            float[] mono = new float[frames];
            for (int f = 0; f < frames; f++) {
                mono[f] = source.getSample(f, ch);
            }

            if (Math.abs(stretch - 1.0) > 1e-3) {
                mono = FourierEngine.timeStretch(mono, sr, stretch, win, hop);
            }

            STFTAnalysis analysis = STFT.INSTANCE.analyze(mono, sr, win, hop);

            if (morph != null && morphMix > 1e-3) {
                float[] morphMono = new float[morph.getFrameCount()];
                for (int f = 0; f < morph.getFrameCount(); f++) {
                    morphMono[f] = morph.getSample(f, Math.min(ch, morph.getChannels() - 1));
                }
                STFTAnalysis morphAnalysis = STFT.INSTANCE.analyze(morphMono, morph.getSampleRate(), win, hop);
                analysis = TimbreTransform.INSTANCE.morphTimbre(analysis, morphAnalysis, morphMix);
            }

            if (Math.abs(tiltDb) > 1e-3) {
                TimbreTransform.INSTANCE.applySpectralTilt(analysis, tiltDb, 1000.0);
            }
            if (Math.abs(formantRatio - 1.0) > 1e-3) {
                TimbreTransform.INSTANCE.applyFormantShift(analysis, formantRatio, 8);
            }
            if (lowCut > 1.0 || highCut < 19900.0) {
                TimbreTransform.INSTANCE.applyBandFilter(analysis, lowCut, highCut);
            }

            processedChannels[ch] = FourierSynthesizer.INSTANCE.synthesize(analysis);
        }

        int outFrames = processedChannels[0].length;
        float[] interleaved = new float[outFrames * channels];
        for (int f = 0; f < outFrames; f++) {
            for (int ch = 0; ch < channels; ch++) {
                interleaved[f * channels + ch] = Math.max(-1f, Math.min(1f, processedChannels[ch][f]));
            }
        }
        return new SampleData(interleaved, sr, channels, source.getRootMidi());
    }

    public static float[] extractMono(SampleData sample) {
        int frames = sample.getFrameCount();
        float[] mono = new float[frames];
        if (sample.getChannels() == 1) {
            for (int i = 0; i < frames; i++) {
                mono[i] = sample.getSample(i, 0);
            }
        } else {
            for (int i = 0; i < frames; i++) {
                mono[i] = 0.5f * (sample.getSample(i, 0) + sample.getSample(i, 1));
            }
        }
        return mono;
    }

    public static void drawSpectrum(Canvas canvas, SpectralModel model, String title) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.setFill(Color.web("#0d1117"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.web("#30363d"));
        gc.strokeRect(0.5, 0.5, w - 1, h - 1);

        if (model == null || model.getFrames().isEmpty()) {
            gc.setFill(Color.web("#8b949e"));
            gc.fillText(title != null ? title : "Spectre FFT", 12, h / 2.0);
            return;
        }

        SpectralModelFrame midFrame = model.getFrames().get(model.getFrames().size() / 2);
        double[] amps = midFrame.getAmplitudes();
        int bins = Math.min(amps.length, 256);

        double maxAmp = 1e-9;
        for (int k = 1; k < bins; k++) {
            if (amps[k] > maxAmp) maxAmp = amps[k];
        }

        double barWidth = w / bins;
        gc.setFill(Color.web("#388bfd"));
        for (int k = 1; k < bins; k++) {
            double db = 20.0 * Math.log10(Math.max(amps[k], 1e-12) / maxAmp);
            double norm = Math.max(0.0, Math.min(1.0, (db + 60.0) / 60.0));
            double barH = norm * (h - 28.0);
            gc.fillRect(k * barWidth, h - barH, Math.max(1.0, barWidth - 0.5), barH);
        }

        // Mettre en évidence les principaux pics sinusoïdaux détectés
        gc.setFill(Color.web("#f78166"));
        List<SinusoidalComponent> comps = midFrame.getComponents();
        int limit = Math.min(5, comps.size());
        for (int i = 0; i < limit; i++) {
            SinusoidalComponent c = comps.get(i);
            int bin = c.getBin();
            if (bin > 0 && bin < bins) {
                double x = bin * barWidth;
                gc.fillOval(x - 3, 24 + i * 14, 6, 6);
                gc.fillText(String.format(Locale.US, "%.0f Hz", c.getFrequency()), x + 6, 31 + i * 14);
            }
        }

        gc.setFill(Color.web("#f0f6fc"));
        gc.fillText(title != null ? title : "Spectre FFT", 10, 16);
    }

    private static class FourierBuildOutput {
        final SampleData sampleData;
        final SamplerInstrument instrument;
        final SpectralModel model;

        FourierBuildOutput(SampleData sampleData, SamplerInstrument instrument, SpectralModel model) {
            this.sampleData = sampleData;
            this.instrument = instrument;
            this.model = model;
        }
    }
}
