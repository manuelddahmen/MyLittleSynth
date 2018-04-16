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
import java.util.function.Consumer;

public class Player extends Thread {
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

    public Player(AudioViewer audioViewer) {
        super();
        soundProductionSystem = new SoundProductionSystem();
        currentNotes = Collections.synchronizedList(new ArrayList<>());
        timer = new Timer();
        timer.init();
        this.audioViewer = audioViewer;
        that = this;
        playing = true;
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
                        double noteTime = note.getTimer().getTimeElapsed();

                        double positionRatioPerSecond = note.getPositionNIncr() / 44100.0;

                        double angle = positionRatioPerSecond * soundProductionSystem.calculateNoteFrequency(note.getTone()) * 2.0 * Math.PI;

                        facteurAmpl = note.getEnveloppe().getVolume(noteTime);

                        double ampl = 32767f * facteurAmpl;

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
                    }
                }
        );
        total /= currentNotes.size() > 0 ? currentNotes.size() : 1;


        short amplitude;

        if (getCurrentNotes().size() > 0) {

            audioViewer.sendDouble(total);
            audioViewer.sendDouble(total);

            amplitude = (short) (total * volume / 100.0);
            //System.out.println(amplitude);

        } else {
            amplitude = 0;
            audioViewer.sendDouble(0.0);
            audioViewer.sendDouble(0.0);
        }
        playBuffer(amplitude);
    }

    public void playBuffer(short amplitude) {
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

    public Note addNote(int tone, double minDuration) {
        Note note = new Note(minDuration, tone, waveform, new Enveloppe(minDuration));

        Timer timer = new Timer();
        note.setTimer(timer);
        timer.init();
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

    void stopNote(Note note) {
        List<Note> notes = getCurrentNotes();
        synchronized (notes) {
            if (getCurrentNotes().contains(note)) {
                note.stop();
                notes.remove(note);

            }
        }
    }

    void playNote(Note note) {
        Platform.runLater(() -> {
                    if (!getCurrentNotes().contains(note)) {
                        addNote(note);
                        note.play();
                    }
                }
        );
    }
}
