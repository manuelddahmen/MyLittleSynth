package one.empty3.audio.sampler

import one.empty3.audio.sampler.core.SampleData
import one.empty3.audio.sampler.core.SampleZone
import one.empty3.audio.sampler.core.SamplerInstrument
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MultisamplerTest {

    private fun createSample(root: Int): SampleData {
        return SampleData(
            samples = FloatArray(44100 * 2),
            sampleRate = 44100,
            channels = 2,
            rootMidi = root
        )
    }

    @Test
    fun testZoneSelectionByMidiNote() {
        val low = SampleZone(
            name = "Low",
            sample = createSample(36),
            keyLow = 36,
            keyHigh = 47
        )

        val high = SampleZone(
            name = "High",
            sample = createSample(60),
            keyLow = 48,
            keyHigh = 72
        )

        val instrument = SamplerInstrument(
            name = "Test Piano",
            zones = listOf(low, high)
        )

        assertEquals(low, instrument.findZone(40, 100))
        assertEquals(high, instrument.findZone(60, 100))
        assertNull(instrument.findZone(80, 100))
    }

    @Test
    fun testVelocityLayers() {
        val soft = SampleZone(
            name = "Soft",
            sample = createSample(60),
            keyLow = 60,
            keyHigh = 60,
            velocityLow = 1,
            velocityHigh = 64
        )

        val hard = SampleZone(
            name = "Hard",
            sample = createSample(60),
            keyLow = 60,
            keyHigh = 60,
            velocityLow = 65,
            velocityHigh = 127
        )

        val instrument = SamplerInstrument(
            name = "Test Piano",
            zones = listOf(soft, hard)
        )

        assertEquals(soft, instrument.findZone(60, 30))
        assertEquals(hard, instrument.findZone(60, 100))
    }

    @Test
    fun testInstrumentPolyphony() {
        val instrument = SamplerInstrument(
            name = "Test",
            zones = listOf(
                SampleZone(
                    name = "Full",
                    sample = createSample(60)
                )
            ),
            polyphony = 8
        )

        assertEquals(8, instrument.polyphony)
    }
}
