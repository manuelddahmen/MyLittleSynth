package one.empty3.apps.mylittlesynth.rythms;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.spi.SoundbankReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by manue on 30-08-19.
 */
public class MidiSoundbankReader extends SoundbankReader
{
    @Override
    public Soundbank getSoundbank(URL url) throws InvalidMidiDataException, IOException {
        return null;
    }

    @Override
    public Soundbank getSoundbank(InputStream stream) throws InvalidMidiDataException, IOException {
        return null;
    }

    @Override
    public Soundbank getSoundbank(File file) throws InvalidMidiDataException, IOException {
        return null;
    }
}
