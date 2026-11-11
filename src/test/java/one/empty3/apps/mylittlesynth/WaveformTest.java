package one.empty3.apps.mylittlesynth;

import one.empty3.apps.mylittlesynth.processor.WaveForm;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WaveformTest {

    private double calculateWaveform(WaveForm wf, double phase, double ampl) {
        switch (wf) {
            case SIN:
                return Math.sin(2.0D * Math.PI * phase) * ampl;
            case RECT:
                return (phase < 0.5D ? 1.0D : -1.0D) * ampl;
            case SAWTOOTH:
                return (2.0D * phase - 1.0D) * ampl;
            case TRI:
                return (4.0D * Math.abs(phase - 0.5D) - 1.0D) * ampl;
            default:
                return Math.sin(2.0D * Math.PI * phase) * ampl;
        }
    }

    @Test
    public void testSineWave() {
        double ampl = Short.MAX_VALUE;
        assertEquals(0.0, calculateWaveform(WaveForm.SIN, 0.0, ampl), 1e-4);
        assertEquals(ampl, calculateWaveform(WaveForm.SIN, 0.25, ampl), 1e-4);
        assertEquals(0.0, calculateWaveform(WaveForm.SIN, 0.5, ampl), 1e-4);
        assertEquals(-ampl, calculateWaveform(WaveForm.SIN, 0.75, ampl), 1e-4);
        assertEquals(0.0, calculateWaveform(WaveForm.SIN, 1.0, ampl), 1e-4);
    }

    @Test
    public void testRectWave() {
        double ampl = Short.MAX_VALUE;
        assertEquals(ampl, calculateWaveform(WaveForm.RECT, 0.0, ampl), 1e-4);
        assertEquals(ampl, calculateWaveform(WaveForm.RECT, 0.25, ampl), 1e-4);
        assertEquals(-ampl, calculateWaveform(WaveForm.RECT, 0.5, ampl), 1e-4);
        assertEquals(-ampl, calculateWaveform(WaveForm.RECT, 0.75, ampl), 1e-4);
    }

    @Test
    public void testSawtoothWave() {
        double ampl = Short.MAX_VALUE;
        assertEquals(-ampl, calculateWaveform(WaveForm.SAWTOOTH, 0.0, ampl), 1e-4);
        assertEquals(0.0, calculateWaveform(WaveForm.SAWTOOTH, 0.5, ampl), 1e-4);
        assertEquals(ampl, calculateWaveform(WaveForm.SAWTOOTH, 1.0, ampl), 1e-4);
    }

    @Test
    public void testTriangleWave() {
        double ampl = Short.MAX_VALUE;
        assertEquals(ampl, calculateWaveform(WaveForm.TRI, 0.0, ampl), 1e-4);
        assertEquals(0.0, calculateWaveform(WaveForm.TRI, 0.25, ampl), 1e-4);
        assertEquals(-ampl, calculateWaveform(WaveForm.TRI, 0.5, ampl), 1e-4);
        assertEquals(0.0, calculateWaveform(WaveForm.TRI, 0.75, ampl), 1e-4);
        assertEquals(ampl, calculateWaveform(WaveForm.TRI, 1.0, ampl), 1e-4);
    }

    @Test
    public void testWaveformBounds() {
        double ampl = Short.MAX_VALUE;
        for (WaveForm wf : new WaveForm[]{WaveForm.SIN, WaveForm.RECT, WaveForm.SAWTOOTH, WaveForm.TRI}) {
            for (double phase = 0.0; phase <= 1.0; phase += 0.01) {
                double val = calculateWaveform(wf, phase, ampl);
                assertTrue(val >= -ampl - 1e-4 && val <= ampl + 1e-4,
                        "Waveform " + wf + " at phase " + phase + " out of bounds: " + val);
            }
        }
    }
}
