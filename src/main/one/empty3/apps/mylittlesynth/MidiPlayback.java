package one.empty3.apps.mylittlesynth;

import com.sun.media.sound.*;

import java.io.File;
import java.io.IOException;
import javax.sound.midi.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MidiPlayback {
    public static void main(String[] args) {
        try {
            Sequencer sequencer = MidiSystem.getSequencer(); // Get the default Sequencer
            if (sequencer==null) {
                System.err.println("Sequencer device not supported");
                return;
            }




            sequencer.open(); // Open device


            Soundbank soundbank = getSoundbank(new File(".\\midibanks\\City Piano.sfz"));

            // Create sequence, the File must contain MIDI file data.


            Sequence sequence = MidiSystem.getSequence(new File("rythmFiles/6054285936689152.mid"));
            sequencer.setSequence(sequence); // load it into sequencer



            sequencer.start();  // start the playback
        } catch (MidiUnavailableException | InvalidMidiDataException | IOException ex) {
            ex.printStackTrace();
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

}