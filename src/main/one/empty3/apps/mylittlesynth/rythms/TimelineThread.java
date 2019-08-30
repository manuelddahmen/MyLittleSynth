package one.empty3.apps.mylittlesynth.rythms;

import com.sun.media.sound.*;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.midi.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

public class TimelineThread extends Thread {


    class PlayMusic extends Thread {
        private final Media sample;

        public PlayMusic(Media sample) {
            this.sample = sample;
        }

        @Override
        public void run() {
            super.run();
            if (sample != null) {
                MediaPlayer mediaPlayer = new MediaPlayer(sample);
                mediaPlayer.play();
                mediaPlayer.dispose();
            }

        }
    }

    class PlayMid extends Thread {
        private final File midi;

        public PlayMid(File wave) {
            this.midi = wave;
        }

        @Override
        public void run() {
            super.run();
            try {
                Sequencer sequencer = MidiSystem.getSequencer(); // Get the default Sequencer
                if (sequencer == null) {
                    System.err.println("Sequencer device not supported");
                    return;
                }
                sequencer.open(); // Open device
                //Soundbank soundbank = getSoundbank(new File(".\\midibanks\\CityPiano.sfz"));
                //MidiSystem.getSynthesizer().loadAllInstruments(soundbank);
                // Create sequence, the File must contain MIDI file data.
                Sequence sequence = MidiSystem.getSequence(midi);
                sequencer.setSequence(sequence); // load it into sequencer
                sequencer.start();  // start the playback
            } catch (MidiUnavailableException | InvalidMidiDataException | IOException ex) {
                ex.printStackTrace();
            }

        }
    }


    public static Soundbank getSoundbank(File file)
            throws InvalidMidiDataException, IOException {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            ais.close();
            ModelByteBufferWavetable osc = new ModelByteBufferWavetable(
                    new ModelByteBuffer(file, 0, file.length()), -4800);
            ModelPerformer performer = new ModelPerformer();
            performer.getOscillators().add(osc);
            SimpleSoundbank sbk = new SimpleSoundbank();
            SimpleInstrument ins = new SimpleInstrument();
            ins.add(performer);
            sbk.addInstrument(ins);
            return sbk;
        } catch (IOException e) {
            return null;
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
            return null;
        }

    }
    private HashMap<URI, Media> samples = new HashMap<>();
    public static final int ENDS = 0;
    public Timeline[] timeline;
    private double TINCR = 0.001;
    private boolean pause = false;

    public int getLoop() {
        return loop;
    }

    public void setLoop(int loop) {
        this.loop = loop;
    }

    private int loop;

    public TimelineThread(Timeline[] tImeline) {
        this.timeline = tImeline;

    }

    public void run() {
        pause = false;
        double t = 0;
        int millis = 100;
        while (isRunning()) {
            while (pause) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            t = getT();
            Timeline.Model prev = null;
            Timeline.Model next = timeline[loop].next(t, prev);

            while (next == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                next = timeline[loop].next(t, prev);
            }

            if (next != null) {

                Media sample = null;
                new PlayMusic(sample).start();
                URI uri = next.wave.toURI();
                if (next.wave.getName().endsWith("mid")) {
                    new PlayMid(next.wave).start();
                } else {
                    if (samples.containsKey(uri))
                        sample = samples.get(uri);
                    else {
                        sample = new Media(next.wave.toURI().toString());
                    }
                }
                if (next.decreasing)
                    next.reminingTimes--;


                else
                    timeline[loop].queue(next);
                timeline[loop].getRythmPanel().textTimeline.setText("" + ((int) t * timeline[loop].getDuration() * 100));

                prev = next;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private double isPlayNow(double t, Timeline.Model next) {
        return next.timeOnTimelinePC * timeline[loop].getDuration();
    }

    private double getT() {
        double t;
        t = timeline[loop].
                getRythmPanel().
                loopTimer[
                timeline[loop].
                        getRythmPanel().
                        loop].
                getCurrentTimeOnLineSec();
        return t;
    }

    private boolean isRunning() {
        return true;
    }

    public void pause() {
        pause = true;
    }
}
