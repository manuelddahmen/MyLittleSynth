package one.empty3.apps.mylittlesynth;

import one.empty3.audio.sampler.core.LoopRegion;
import one.empty3.audio.sampler.core.SampleData;
import one.empty3.audio.sampler.core.Sampler;
import one.empty3.audio.sampler.core.WavReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

public class SamplerIntegrationTest {

    private byte[] createSineWavBytes(int sampleRate, int frameCount, double freqHz) {
        int channels = 1;
        int bitsPerSample = 16;
        int blockAlign = channels * (bitsPerSample / 8);
        int byteRate = sampleRate * blockAlign;
        int dataSize = frameCount * blockAlign;

        ByteBuffer bb = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN);
        bb.put(new byte[]{'R', 'I', 'F', 'F'});
        bb.putInt(36 + dataSize);
        bb.put(new byte[]{'W', 'A', 'V', 'E'});
        bb.put(new byte[]{'f', 'm', 't', ' '});
        bb.putInt(16);
        bb.putShort((short) 1); // PCM
        bb.putShort((short) channels);
        bb.putInt(sampleRate);
        bb.putInt(byteRate);
        bb.putShort((short) blockAlign);
        bb.putShort((short) bitsPerSample);
        bb.put(new byte[]{'d', 'a', 't', 'a'});
        bb.putInt(dataSize);

        for (int i = 0; i < frameCount; i++) {
            double t = i / (double) sampleRate;
            short sample = (short) (Math.sin(2.0 * Math.PI * freqHz * t) * 24000);
            bb.putShort(sample);
        }
        return bb.array();
    }

    @Test
    public void testWavReaderAndChromaticSampler() {
        byte[] wav = createSineWavBytes(44100, 4410, 261.63);
        SampleData sample = WavReader.read(new ByteArrayInputStream(wav), 60);

        assertEquals(44100, sample.getSampleRate());
        assertEquals(1, sample.getChannels());
        assertEquals(4410, sample.getFrameCount());
        assertEquals(60, sample.getRootMidi());

        Sampler sampler = new Sampler(null, 44100, 8);
        assertFalse(sampler.hasAnySample());

        sampler.setRootSample(sample, new LoopRegion(100, 2000, true));
        assertTrue(sampler.hasAnySample());
        assertTrue(sampler.hasSampleForNote(60));
        assertTrue(sampler.hasSampleForNote(67));

        sampler.noteOn(60, 1.0f);
        float[] output = new float[512 * 2];
        sampler.render(output, 512);

        double energy = 0.0;
        for (float v : output) {
            energy += Math.abs(v);
        }
        assertTrue(energy > 0.1, "Sampler should produce non-zero audio output for active note");
    }

    @Test
    public void testSingleNoteAssignmentMode() {
        byte[] wav = createSineWavBytes(44100, 2205, 440.0);
        SampleData sample = WavReader.decode(wav, 69);

        Sampler sampler = new Sampler(null, 44100, 8);
        sampler.setChromaticMode(false);
        sampler.assignNoteSample(69, sample, null);

        assertTrue(sampler.hasSampleForNote(69));
        assertFalse(sampler.hasSampleForNote(60));

        // Unassigned note 60 should remain silent in single-note mode
        sampler.noteOn(60, 1.0f);
        float[] output = new float[256 * 2];
        sampler.render(output, 256);
        for (float v : output) {
            assertEquals(0.0f, v, 1e-6f);
        }

        // Assigned note 69 should produce sound
        sampler.noteOn(69, 1.0f);
        sampler.render(output, 256);
        double energy = 0.0;
        for (float v : output) {
            energy += Math.abs(v);
        }
        assertTrue(energy > 0.1, "Assigned note 69 should produce sound");
    }

    @Test
    public void testMidiAndNoteConversions() {
        assertEquals(60, SoundProductionSystem.noteToMidi("C4"));
        assertEquals(61, SoundProductionSystem.noteToMidi("C#4"));
        assertEquals(61, SoundProductionSystem.noteToMidi("C4#"));
        assertEquals(69, SoundProductionSystem.noteToMidi("A4"));
        assertEquals(60, SamplerWizardDialog.computeMidiNote(0, 4));
        assertEquals(69, SamplerWizardDialog.computeMidiNote(9, 4));
        assertEquals("C4 (do 4)", SoundProductionSystem.midiToNoteName(60));
    }
}
