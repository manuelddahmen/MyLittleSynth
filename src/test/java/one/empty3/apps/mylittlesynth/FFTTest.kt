package one.empty3.audio.sampler.dsp

import one.empty3.audio.sampler.core.SampleData
import one.empty3.audio.sampler.core.Sampler
import one.empty3.audio.sampler.dsp.analyzer.SignalAnalyzer
import one.empty3.audio.sampler.dsp.stft.STFT
import one.empty3.audio.sampler.dsp.stft.WindowType
import one.empty3.audio.sampler.dsp.synth.FourierSynthesizer
import one.empty3.audio.sampler.dsp.transform.TimbreTransform
import one.empty3.audio.sampler.dsp.vocoder.PhaseVocoder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class FFTTest {

    private fun sineWave(sampleRate: Int, length: Int, freqHz: Double, amp: Double = 0.5): FloatArray {
        return FloatArray(length) { i ->
            (amp * sin(2.0 * PI * freqHz * i / sampleRate)).toFloat()
        }
    }

    @Test
    fun testStftReconstruction() {
        val input = sineWave(48000, 48000, 440.0, 0.5)

        val analysis = STFT.analyze(input, 48000)
        val output = STFT.synthesize(analysis)

        assertEquals(input.size, output.size)

        var maxError = 0f

        for (i in input.indices) {
            val error = abs(input[i] - output[i])
            if (error > maxError) {
                maxError = error
            }
        }

        assertTrue(maxError < 1e-4f, "Expected maxError < 1e-4, got $maxError")
    }

    @Test
    fun testSignalAnalyzerAndSpectralModel() {
        val sampleRate = 44100
        val input = FloatArray(16384) { i ->
            (0.6 * sin(2.0 * PI * 440.0 * i / sampleRate) +
                0.3 * sin(2.0 * PI * 880.0 * i / sampleRate)).toFloat()
        }

        val model = SignalAnalyzer.analyze(
            samples = input,
            sampleRate = sampleRate,
            windowSize = 2048,
            hopSize = 512,
            windowType = WindowType.HANN
        )

        assertTrue(model.frames.isNotEmpty())
        val midFrame = model.frames[model.frames.size / 2]
        assertTrue(midFrame.components.size >= 2)
        assertEquals(440.0, midFrame.components[0].frequency, 3.0)
        assertEquals(880.0, midFrame.components[1].frequency, 3.0)
        assertEquals(2048, midFrame.residual.size)

        val reconstructed = FourierSynthesizer.synthesize(model)
        assertEquals(input.size, reconstructed.size)
    }

    @Test
    fun testSpectralTransformTiltAndFormants() {
        val sampleRate = 44100
        val input = FloatArray(8192) { i ->
            (0.4 * sin(2.0 * PI * 300.0 * i / sampleRate) +
                0.4 * sin(2.0 * PI * 2400.0 * i / sampleRate)).toFloat()
        }

        val analysis = STFT.analyze(input, sampleRate, windowSize = 1024, hopSize = 256)
        TimbreTransform.applySpectralTilt(analysis, dbPerOctave = 6.0, referenceHz = 1000.0)
        TimbreTransform.applyFormantShift(analysis, shiftRatio = 1.15)
        val output = FourierSynthesizer.synthesize(analysis)

        assertEquals(input.size, output.size)
        val peak = output.maxOf { abs(it) }
        assertTrue(peak > 0.05f, "Transformed signal should have non-zero energy")

        val other = sineWave(sampleRate, 8192, 660.0, 0.5)
        val morphed = TimbreTransform.morphTimbre(
            STFT.analyze(input, sampleRate, 1024, 256),
            STFT.analyze(other, sampleRate, 1024, 256),
            0.5
        )
        val morphedOut = FourierSynthesizer.synthesize(morphed)
        assertEquals(input.size, morphedOut.size)
    }

    @Test
    fun testPhaseVocoderTimeStretchAndPitchShift() {
        val sampleRate = 44100
        val input = sineWave(sampleRate, 16384, 440.0, 0.5)

        val stretched = PhaseVocoder.timeStretch(input, sampleRate, stretchFactor = 1.5, windowSize = 1024, hopSize = 256)
        assertTrue(abs(stretched.size - (input.size * 1.5).toInt()) <= 2)

        val shifted = PhaseVocoder.pitchShift(
            samples = input,
            sampleRate = sampleRate,
            semitones = 12.0,
            windowSize = 1024,
            hopSize = 256
        )
        assertEquals(input.size, shifted.size)

        // Verify dominant frequency moved from 440 Hz up one octave to ~880 Hz
        val shiftedModel = SignalAnalyzer.analyze(shifted, sampleRate, windowSize = 2048, hopSize = 512)
        val midFrame = shiftedModel.frames[shiftedModel.frames.size / 2]
        assertTrue(midFrame.components.isNotEmpty())
        assertEquals(880.0, midFrame.components[0].frequency, 25.0)
    }

    @Test
    fun testFourierSynthesizerMultisamplerIntegration() {
        val sampleRate = 44100
        val mono = sineWave(sampleRate, 8192, 261.63, 0.6)
        val baseSample = SampleData(mono, sampleRate, channels = 1, rootMidi = 60)

        val instrument = FourierEngine.buildInstrument(
            name = "FourierTestSynth",
            baseSample = baseSample,
            anchorMidiNotes = listOf(48, 60, 72),
            spectralTiltDb = 3.0,
            polyphony = 8
        )

        assertEquals(3, instrument.zones.size)
        val sampler = Sampler(instrument, sampleRate)
        sampler.noteOn(60, 1.0f)
        sampler.noteOn(72, 0.8f)

        val out = FloatArray(512 * 2)
        sampler.render(out, 512)
        val energy = out.sumOf { abs(it).toDouble() }
        assertTrue(energy > 0.1, "Multisampler driven by FourierEngine should produce audio")
    }
}
