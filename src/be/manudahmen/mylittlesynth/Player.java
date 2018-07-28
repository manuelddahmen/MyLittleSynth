/*
 * This file is part of Plants-Growth-2
 *     Plants-Growth-2 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Plants-Growth-2 is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Plants-Growth-2.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.manudahmen.mylittlesynth;

import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player extends Thread {
    private final App app;
    private List<NoteState> notesRecorded;

    public List<NoteState> getNoteStates() {
        return noteStates;
    }

    private final List<NoteState> noteStates;
    private List<Note> currentNotes;
    private Timer timer;
    private boolean playing;
    private SoundProductionSystem soundProductionSystem;
    private Player that;
    private AudioViewer audioViewer;
    private int octave = 4;
    private String form;
    private SoundProductionSystem.Waveform waveform = SoundProductionSystem.Waveform.SIN;
    private double volume = 100;
    private long position;
    private boolean recording;
    private boolean playingBuffer = false;
    private boolean loopPlayingBuffer = false;
    private NoteTimer timerRecording = new NoteTimer();

    public Player(App app, AudioViewer audioViewer) {
        super();
        this.app = app;
        soundProductionSystem = new SoundProductionSystem();
        currentNotes = Collections.synchronizedList(new ArrayList<>());
        noteStates = Collections.synchronizedList(new ArrayList<>());
        timer = new Timer();
        timer.init();
        this.audioViewer = audioViewer;
        that = this;
        playing = true;
        position = 0;
    }

    public void addNote(Note note) {
        currentNotes.add(note);

    }

    double total = 0;
    double facteurAmpl = 0;
    Short a = 0;

    public void playCurrentNotes() {
        total = 0.0;
        getCurrentNotes().forEach(note -> {
                    if (!note.isFinish()) {
                        double noteTime = note.getTimer().getTotalTimeElapsed() / 1E9;

                        double positionRatioPerSecond = note.getPosition() * 44100;

                        note.positionInc();

                        double angle = positionRatioPerSecond * soundProductionSystem.calculateNoteFrequency(note.getTone()) * 2.0 * Math.PI;

                        facteurAmpl = note.getEnveloppe().getVolume(noteTime);


                        double ampl = 32767d * facteurAmpl;

                        switch (note.getWaveform()) {
                            case SIN: // SIN
                                total += (Math.sin(angle) * ampl);  //32767 - max value for sample to take (-32767 to 32767)
                                break;
                            case RECT: // RECT
                                total += (Math.signum(Math.sin(angle)) * ampl);  //32767 - max value for sample to take (-32767 to 32767)
                            case SAWTOOTH: // SAWTOOTH LINEAR
                                total += ((1 - angle / 2 * Math.PI) * ampl);  //32767 - max value for sample to take (-32767 to 32767)
                            case TRI: // TRIANGLE LINEAR
                                total += ((1 - Math.abs(angle / 2 * Math.PI) * ampl));  //32767 - max value for sample to take (-32767 to 32767)
                            default: // SIN
                                total += (Math.sin(angle) * ampl);  //32767 - max value for sample to take (-32767 to 32767)
                                break;

                        }

                        audioViewer.sendEnvelopeVolume(note.getTone(), note.getEnveloppe().getBrutVolume(noteTime));

                    }
                }
        );
        total /= currentNotes.size() > 0 ? currentNotes.size() : 1;


        short amplitude;

        if (getCurrentNotes().size() > 0) {
            amplitude = (short) (total * volume / 100.0);

            audioViewer.sendDouble(amplitude * 1.);
            audioViewer.sendDouble(amplitude * 1.);


            //System.out.println(amplitude);

        } else {
            amplitude = 0;
            audioViewer.sendDouble(0.0);
            audioViewer.sendDouble(0.0);
        }
        playBufferMono(amplitude);
        position++;

    }

    public void playBufferMono(short amplitude) {
        short a = amplitude;
        byte[] nextBuffer = new byte[4];
        nextBuffer[0] = (byte) (a & 0xFF); //write 8bits ________WWWWWWWW out of 16
        nextBuffer[1] = (byte) (a >> 8); //write 8bits WWWWWWWW________ out of 16
        nextBuffer[2] = (byte) (a & 0xFF); //write 8bits ________WWWWWWWW out of 16
        nextBuffer[3] = (byte) (a >> 8); //write 8bits WWWWWWWW________ out of 16
        try {
            soundProductionSystem.getLine().write(nextBuffer, 0, 4);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        while (isPlaying()) {
            playCurrentNotes();
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public Note addNote(int tone, long minDuration) {
        Note note = new Note(minDuration, tone, waveform, new Enveloppe(minDuration));
        note.getTimer().init();
        Platform.runLater(() -> {
            getCurrentNotes().add(note);
            System.out.println("After added " + getCurrentNotes().size());
        });

        return note;
    }

    public void stopNote(int tone) {
        getCurrentNotes().forEach(note -> {
            if (note.getTone() == tone) {
                Platform.runLater(() -> {
                    //while (!note.isFinish())
                    ;
                    getCurrentNotes().remove(note);
                    System.out.println("After removed " + getCurrentNotes().size());
                });
            }
        });
    }

    public List<Note> getCurrentNotes() {
        return currentNotes;
    }

    public void setOctave(int octave) {
        this.octave = octave;
    }

    public int getOctave() {
        return octave;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public void setWaveform(SoundProductionSystem.Waveform waveform) {
        this.waveform = waveform;
    }

    public SoundProductionSystem.Waveform getForm() {
        return waveform;

    }

    public void setVolume(double value) {
        this.volume = value;
    }

    public double getVolume() {
        return volume;
    }

    public void stopNote(Note note) {
        List<Note> notes = getCurrentNotes();
        synchronized (notes) {
            if (getCurrentNotes().contains(note)) {
                note.stop();
                notes.remove(note);
                if (isRecording()) {
                    NoteState noteState = new NoteState(
                            note, timerRecording.getTotalTimeElapsed(),
                            false);
                    timerRecording.add(noteState);
                }

            }
        }
    }

    public void playNote(Note note) {
        Platform.runLater(() -> {
                    if (!getCurrentNotes().contains(note)) {
                        addNote(note);
                        note.play();
                        if (isRecording()) {
                            NoteState noteState = new NoteState(
                                    note, timerRecording.getTotalTimeElapsed(),
                                    true);
                            timerRecording.add(noteState);
                        }
                    }
                }
        );
    }


    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        System.out.println("Recording: " + recording);

        this.recording = recording;
        if (isRecording()) {
            timerRecording.stop();
            timerRecording.init();
            setPlayingBuffer(true);
        } else {
            timerRecording.stop();
        }
    }

    private void playNoteBuffer(List<NoteState> notesRecorded) {
        new RepeatThread(this) {

        }.start();

    }

    public void setPlayingBuffer(boolean playingBuffer) {
        this.playingBuffer = playingBuffer;
    }

    public boolean isPlayingBuffer() {
        return playingBuffer;
    }

    public void toggleRecording() {
        recording = !recording;
        if (!isRecording()) {
            setPlayingBuffer(true);
        }
        setRecording(recording);
    }

    public boolean isLoopPlayingBuffer() {
        return loopPlayingBuffer;
    }

    public void setLoopPlayingBuffer(boolean loopPlayingBuffer) {
        this.loopPlayingBuffer = loopPlayingBuffer;
    }

    public NoteTimer getTimerRecording() {
        return timerRecording;
    }

    public void setTimerRecording(NoteTimer timerRecording) {
        this.timerRecording = timerRecording;
    }

    public List<NoteState> getNotesRecorded() {
        return notesRecorded;
    }

    public App getApp() {
        return app;
    }
}
