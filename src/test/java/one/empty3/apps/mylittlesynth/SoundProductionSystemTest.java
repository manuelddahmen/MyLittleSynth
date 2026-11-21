package one.empty3.apps.mylittlesynth;

import one.empty3.audio.sampler.core.SampleData;
import one.empty3.audio.sampler.core.WavReader;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileInputStream;
import static org.junit.jupiter.api.Assertions.*;

public class SoundProductionSystemTest {

    @Test
    public void testFrequencyCalculation() {
        // A4 (tone 0 relative to 440Hz base)
        assertEquals(440.0f, SoundProductionSystem.getFrequency(0), 0.01f);
        // A5 (tone 12) = 880.0Hz
        assertEquals(880.0f, SoundProductionSystem.getFrequency(12), 0.01f);
        // A3 (tone -12) = 220.0Hz
        assertEquals(220.0f, SoundProductionSystem.getFrequency(-12), 0.01f);
        // C4 (tone -9) = 261.63Hz
        assertEquals(261.63f, SoundProductionSystem.getFrequency(-9), 0.1f);
    }

    @Test
    public void testEquivNoteParsing() {
        assertEquals(44, SoundProductionSystem.equiv("A4"));
        assertEquals(35, SoundProductionSystem.equiv("C4"));
        assertEquals(36, SoundProductionSystem.equiv("C#4"));
        assertEquals(45, SoundProductionSystem.equiv("Bb4"));
        assertEquals(0, SoundProductionSystem.equiv(null));
        assertEquals(0, SoundProductionSystem.equiv(""));
    }

    @Test
    public void testWavRecordingWriteAndReadBack() throws Exception {
        SoundProductionSystem sps = new SoundProductionSystem(2.0f);
        try {
            assertFalse(sps.isRecording());
            sps.startRecording();
            assertTrue(sps.isRecording());

            // 1024 stereo 16-bit PCM frames = 4096 bytes
            byte[] pcm = new byte[1024 * 4];
            for (int f = 0; f < 1024; f++) {
                short s = (short) (Math.sin(2.0 * Math.PI * 440.0 * f / 44100.0) * 20000);
                pcm[f * 4] = (byte) (s & 0xff);
                pcm[f * 4 + 1] = (byte) (s >> 8);
                pcm[f * 4 + 2] = (byte) (s & 0xff);
                pcm[f * 4 + 3] = (byte) (s >> 8);
            }
            sps.writeWaveBuffer(pcm);

            File saved = sps.stopRecordingAndWrite();
            assertFalse(sps.isRecording());
            assertNotNull(saved);
            assertTrue(saved.exists());
            assertTrue(saved.getName().startsWith("outputWave-"));
            assertTrue(saved.getName().endsWith("record.wav"));

            try (FileInputStream fis = new FileInputStream(saved)) {
                SampleData decoded = WavReader.read(fis, 60);
                assertEquals(44100, decoded.getSampleRate());
                assertEquals(2, decoded.getChannels());
                assertEquals(1024, decoded.getFrameCount());
            } finally {
                saved.delete();
            }
        } finally {
            sps.end();
        }
    }
}
