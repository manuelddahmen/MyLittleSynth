package one.empty3.apps.mylittlesynth;

import one.empty3.apps.mylittlesynth.processor.WaveForm;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.application.Platform;

import javax.sound.midi.*;

public class PlayerSwing extends Thread implements PropertyChangeListener {
    private final AppNew app;
    private int channelIndex;
    private Receiver synthRcvr;
    private List notesRecorded;
    private final List noteStates;
    private List currentNotes;
    private Timer timer;
    private boolean playing;
    private PlayerSwing that;
    //private AudioViewer audioViewer;
    private int octave = 4;
    private String form;
    private WaveForm waveform = WaveForm.SIN;
    private double volume;
    private long position;
    private boolean recording;
    private boolean playingBuffer;
    private boolean loopPlayingBuffer;
    private NoteTimer timerRecording;
    double total;
    double facteurAmpl;
    Short a;
    byte[] nextBuffer;
    private short[] amplitude = new short[1024];
    private Synthesizer synth;
    private int lastInst = -1;

    public List<NoteState> getNoteStates() {
        return this.noteStates;
    }

    public PlayerSwing(AppNew app) {
        this.waveform = WaveForm.SIN;
        this.volume = 100.0D;
        this.playingBuffer = false;
        this.loopPlayingBuffer = false;
        this.timerRecording = new NoteTimer();
        this.total = 0.0D;
        this.facteurAmpl = 30000D;
        this.a = Short.valueOf((short)0);
        this.nextBuffer = new byte[4];
        this.app = app;
        this.currentNotes = Collections.synchronizedList(new ArrayList());
        this.noteStates = Collections.synchronizedList(new ArrayList());
        this.timer = new Timer();
        //this.audioViewer = audioViewer;
        this.that = this;
        this.playing = true;

        try {
            synth = MidiSystem.getSynthesizer();

            synth.open();

            synthRcvr = synth.getReceiver();

            MidiChannel[] channels = synth.getChannels();
            for (MidiChannel channel : channels) {
                channelIndex = 0;
            }

        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }

    }

    public void addNote(Note note) {
        this.currentNotes.add(note);
    }

    public void playCurrentNotes() {
        List<Note> activeNotes;
        synchronized (this.currentNotes) {
            if (this.currentNotes.isEmpty()) {
                return;
            }
            activeNotes = new ArrayList<>(this.currentNotes);
        }

        double sampleRate = 44100.0D;
        if (this.app != null && this.app.getKeyContainer() != null && this.app.getKeyContainer().getSoundProductionSystem() != null) {
            sampleRate = this.app.getKeyContainer().getSoundProductionSystem().sampleRate;
        }
        if (sampleRate <= 0) {
            sampleRate = 44100.0D;
        }
        double ampl = Short.MAX_VALUE;

        for (int t = 0; t < amplitude.length; t++) {
            double sampleSum = 0.0D;
            for (Note note : activeNotes) {
                note.setNoteTimeSamples(note.getNoteTimeSamples() + 1);
                double noteTimeSec = (double) note.getNoteTimeSamples() / sampleRate;
                double freq = SoundProductionSystem.getFrequency(note.getTone());
                double cycles = freq * noteTimeSec;
                double phase = cycles - Math.floor(cycles);
                double noteSample;

                WaveForm wf = note.getWaveform();
                if (wf == null) {
                    wf = WaveForm.SIN;
                }

                switch (wf) {
                    case SIN:
                        noteSample = Math.sin(2.0D * Math.PI * phase) * ampl;
                        break;
                    case RECT:
                        noteSample = (phase < 0.5D ? 1.0D : -1.0D) * ampl;
                        break;
                    case SAWTOOTH:
                        noteSample = (2.0D * phase - 1.0D) * ampl;
                        break;
                    case TRI:
                        noteSample = (4.0D * Math.abs(phase - 0.5D) - 1.0D) * ampl;
                        break;
                    default:
                        noteSample = Math.sin(2.0D * Math.PI * phase) * ampl;
                        break;
                }
                sampleSum += noteSample;
            }

            sampleSum /= (double) activeNotes.size();
            double scaled = sampleSum * (this.volume / 100.0D);
            if (scaled > Short.MAX_VALUE) {
                scaled = Short.MAX_VALUE;
            } else if (scaled < Short.MIN_VALUE) {
                scaled = Short.MIN_VALUE;
            }
            amplitude[t] = (short) ((int) scaled);
        }

        this.playBufferMono(amplitude);
        position += amplitude.length;
    }

    private void playBufferMono(short[] amplitude) {
        nextBuffer = new byte[amplitude.length * 4];
        for (int t = 0; t < amplitude.length; t++) {
            this.nextBuffer[4 * t + 0] = (byte) (amplitude[t] & 255);
            this.nextBuffer[4 * t + 1] = (byte) (amplitude[t] >> 8);
            this.nextBuffer[4 * t + 2] = (byte) (amplitude[t] & 255);
            this.nextBuffer[4 * t + 3] = (byte) (amplitude[t] >> 8);
        }
        try {
            if (this.app != null && this.app.getKeyContainer() != null && this.app.getKeyContainer().getSoundProductionSystem() != null) {
                this.app.getKeyContainer().getSoundProductionSystem().getLine().write(this.nextBuffer, 0, 4 * amplitude.length);
                this.app.getKeyContainer().getSoundProductionSystem().writeWaveBuffer(this.nextBuffer);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    public void run() {
        while(this.isPlaying()) {
            if (this.currentNotes.isEmpty()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            } else {
                this.playCurrentNotes();
            }
        }

    }

    public boolean isPlaying() {
        return this.playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public Note addNote(int tone, long minDurationSec) {
        Note note = new Note(null, (double)minDurationSec, tone, this.waveform);
        Platform.runLater(() -> {
            this.getCurrentNotes().add(note);
            System.out.println("After added " + this.getCurrentNotes().size());
        });
        return note;
    }

    public void stopNote(int tone) {
        List<Note> toStop = new ArrayList<>();
        synchronized (this.currentNotes) {
            for (Object note1 : this.currentNotes) {
                Note note = (Note) note1;
                if (note.getTone() == tone) {
                    toStop.add(note);
                }
            }
        }
        for (Note n : toStop) {
            this.stopNote(n);
        }
    }

    public List getCurrentNotes() {
        return this.currentNotes;
    }

    public void setOctave(int octave) {
        this.octave = octave;
    }

    public int getOctave() {
        return this.octave;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public void setWaveform(WaveForm waveform) {
        this.waveform = waveform;
    }

    public WaveForm getForm() {
        return this.waveform;
    }

    public void setVolume(double value) {
        this.volume = value;
    }

    public double getVolume() {
        return this.volume;
    }

    public void stopNote(Note note) {
        List notes = this.getCurrentNotes();
        synchronized(notes) {
            if (this.getCurrentNotes().contains(note)) {
                note.stop();
                notes.remove(note);
                if (this.isRecording()) {
                    NoteState noteState = new NoteState(note, this.timerRecording.getTotalTimeElapsedNanoSec(), false);
                    this.timerRecording.add(noteState);
                }
            }

        }
    }

    public void playNote(Note2 note) {
        new Thread(){
            @Override
            public void run() {
                if (!getCurrentNotes().contains(note)) {
                    if(note.getInstrument()>=0)
                    {
                        try {
                            ShortMessage myMsg = new ShortMessage();
                            // Play the note Middle C (60) moderately loud
                            // (velocity = 93)on channel 4 (zero-based).
                            myMsg.setMessage(ShortMessage.NOTE_ON, channelIndex, note.getTone(), 93);
                            Instrument instr;
                            if(note.getInstrument()!=lastInst) {
                                synth.loadInstrument(instr = synth.getAvailableInstruments()[note.getInstrument()]);
                                synth.getChannels()[channelIndex].programChange(instr.getPatch().getProgram());
                                lastInst = note.getInstrument();
                            }
                            synthRcvr.send(myMsg, -1); // -1 means no time stamp
                        } catch (InvalidMidiDataException e) {
                            e.printStackTrace();
                        }
                    } else {
                        note.setWaveform(getForm());
                        addNote(note);
                        note.play();
                        if (isRecording()) {
                            NoteState noteState = new NoteState(note, timerRecording.getTotalTimeElapsedNanoSec(), true);
                            timerRecording.add(noteState);
                        }
                    }
                }

            }
        }.start();
    }

    public boolean isRecording() {
        return this.recording;
    }

    public void setRecording(boolean recording) {
        System.out.println("Recording: " + recording);
        this.recording = recording;
        if (this.isRecording()) {
            this.timerRecording.stop();
            this.setPlayingBuffer(true);
        } else {
            this.timerRecording.stop();
        }

    }

    private void playNoteBuffer(List notesRecorded) {
        //(new RepeatThread(this) {
        //}).start();
    }

    public void setPlayingBuffer(boolean playingBuffer) {
        this.playingBuffer = playingBuffer;
    }

    public boolean isPlayingBuffer() {
        return this.playingBuffer;
    }

    public void toggleRecording() {
        this.recording = !this.recording;
        if (!this.isRecording()) {
            this.setPlayingBuffer(true);
        }

        this.setRecording(this.recording);
    }

    public boolean isLoopPlayingBuffer() {
        return this.loopPlayingBuffer;
    }

    public void setLoopPlayingBuffer(boolean loopPlayingBuffer) {
        this.loopPlayingBuffer = loopPlayingBuffer;
    }

    public NoteTimer getTimerRecording() {
        return this.timerRecording;
    }

    public void setTimerRecording(NoteTimer timerRecording) {
        this.timerRecording = timerRecording;
    }

    public List getNotesRecorded() {
        return this.notesRecorded;
    }

    public AppNew getApp() {
        return this.app;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        evt.getNewValue();
    }
}
