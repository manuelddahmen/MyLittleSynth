package one.empty3.audio.sampler.dsp

import one.empty3.audio.sampler.core.AdsrSettings
import one.empty3.audio.sampler.core.LoopRegion
import one.empty3.audio.sampler.core.SampleData
import one.empty3.audio.sampler.core.SamplerInstrument
import one.empty3.audio.sampler.dsp.analyzer.SignalAnalyzer
import one.empty3.audio.sampler.dsp.model.SpectralModel
import one.empty3.audio.sampler.dsp.stft.STFT
import one.empty3.audio.sampler.dsp.stft.STFTAnalysis
import one.empty3.audio.sampler.dsp.stft.WindowType
import one.empty3.audio.sampler.dsp.synth.FourierSynthesizer
import one.empty3.audio.sampler.dsp.transform.TimbreTransform
import one.empty3.audio.sampler.dsp.vocoder.PhaseVocoder

/**
 * Empty3 Fourier Engine — Façade unifiée des 5 étages DSP :
 * 1. Signal Analyzer (FFT, STFT, fenêtres, détection de composantes)
 * 2. Spectral Model (amplitudes, phases, fréquences instantanées, résidu)
 * 3. Spectral Transform (transposition, enveloppe spectrale, formants, timbres)
 * 4. Phase Vocoder (transposition indépendante de la durée, continuité des phases)
 * 5. Audio Synthesizer (IFFT, Overlap-Add, mixage, intégration multisampler)
 */
object FourierEngine {

    @JvmStatic
    @JvmOverloads
    fun analyze(
        samples: FloatArray,
        sampleRate: Int,
        windowSize: Int = 2048,
        hopSize: Int = 512,
        windowType: WindowType = WindowType.HANN
    ): SpectralModel {
        return SignalAnalyzer.analyze(
            samples = samples,
            sampleRate = sampleRate,
            windowSize = windowSize,
            hopSize = hopSize,
            windowType = windowType
        )
    }

    @JvmStatic
    @JvmOverloads
    fun transformTimbre(
        samples: FloatArray,
        sampleRate: Int,
        spectralTiltDb: Double = 0.0,
        formantShiftRatio: Double = 1.0,
        lowCutHz: Double = 0.0,
        highCutHz: Double = Double.POSITIVE_INFINITY,
        windowSize: Int = 2048,
        hopSize: Int = 512
    ): FloatArray {
        val analysis: STFTAnalysis = STFT.analyze(samples, sampleRate, windowSize, hopSize)
        if (kotlin.math.abs(spectralTiltDb) > 1e-6) {
            TimbreTransform.applySpectralTilt(analysis, spectralTiltDb)
        }
        if (kotlin.math.abs(formantShiftRatio - 1.0) > 1e-6) {
            TimbreTransform.applyFormantShift(analysis, formantShiftRatio)
        }
        if (lowCutHz > 0.0 || highCutHz.isFinite()) {
            TimbreTransform.applyBandFilter(analysis, lowCutHz, highCutHz)
        }
        return FourierSynthesizer.synthesize(analysis)
    }

    @JvmStatic
    @JvmOverloads
    fun pitchShift(
        samples: FloatArray,
        sampleRate: Int,
        semitones: Double,
        preserveFormants: Boolean = false,
        windowSize: Int = 2048,
        hopSize: Int = 512
    ): FloatArray {
        return PhaseVocoder.pitchShift(
            samples = samples,
            sampleRate = sampleRate,
            semitones = semitones,
            windowSize = windowSize,
            hopSize = hopSize,
            preserveFormants = preserveFormants
        )
    }

    @JvmStatic
    @JvmOverloads
    fun timeStretch(
        samples: FloatArray,
        sampleRate: Int,
        stretchFactor: Double,
        windowSize: Int = 2048,
        hopSize: Int = 512
    ): FloatArray {
        return PhaseVocoder.timeStretch(
            samples = samples,
            sampleRate = sampleRate,
            stretchFactor = stretchFactor,
            windowSize = windowSize,
            hopSize = hopSize
        )
    }

    @JvmStatic
    @JvmOverloads
    fun buildInstrument(
        name: String,
        baseSample: SampleData,
        anchorMidiNotes: List<Int>,
        preserveFormants: Boolean = false,
        spectralTiltDb: Double = 0.0,
        formantShiftRatio: Double = 1.0,
        polyphony: Int = 32,
        envelope: AdsrSettings = AdsrSettings(),
        loop: LoopRegion? = null
    ): SamplerInstrument {
        return FourierSynthesizer.buildMultisamplerInstrument(
            name = name,
            baseSample = baseSample,
            anchorMidiNotes = anchorMidiNotes,
            preserveFormants = preserveFormants,
            spectralTiltDb = spectralTiltDb,
            formantShiftRatio = formantShiftRatio,
            polyphony = polyphony,
            envelope = envelope,
            loop = loop
        )
    }
}
