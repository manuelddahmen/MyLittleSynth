package one.empty3.apps.mylittlesynth;

import org.junit.jupiter.api.Test;
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
}
