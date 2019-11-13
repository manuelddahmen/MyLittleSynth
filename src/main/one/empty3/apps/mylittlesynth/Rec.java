package one.empty3.apps.mylittlesynth;

import javax.sound.midi.MidiSystem;
import java.io.File;

/**
 * Created by manue on 28-10-19.
 */
public class Rec {

    private final long timeNano;
    private File f;

    public Rec()
    {
        f = new File("random/"+System.nanoTime());
        f.mkdirs();
        this.timeNano = System.nanoTime();
    }


    public void addNote(Note2 newValue) {

    }
}
