package one.empty3.audio.sampler.dsp.synth

import one.empty3.audio.sampler.core.AdsrSettings
import one.empty3.audio.sampler.core.LoopRegion
import one.empty3.audio.sampler.core.SampleData
import one.empty3.audio.sampler.core.SampleZone
import one.empty3.audio.sampler.core.SamplerInstrument
import one.empty3.audio.sampler.dsp.model.SpectralModel
import one.empty3.audio.sampler.dsp.stft.STFT
import one.empty3.audio.sampler.dsp.stft.STFTAnalysis
import one.empty3.audio.sampler.dsp.stft.WindowFunctions
import one.empty3.audio.sampler.dsp.transform.TimbreTransform
import one.empty3.audio.sampler.dsp.vocoder.PhaseVocoder
import kotlin.math.abs
import kotlin.math.max

/**
 * 5. Audio Synthesizer
 * Synthèse IFFT + Overlap-Add, reconstruction sinusoïdale + résidu,
 * mixage multi-pistes et intégration directe au multisampler (SamplerInstrument / SampleZone).
 */
object FourierSynthesizer {

    /**
     * Synthétise un signal mono depuis une analyse STFT par IFFT + Overlap-Add.
     */
    fun synthesize(analysis: STFTAnalysis): FloatArray {
        return STFT.synthesize(analysis)
    }

    /**
     * Synthétise un signal mono depuis un SpectralModel complet,
     * permettant de doser la partie spectrale/sinusoïdale et le résidu.
     */
    @JvmOverloads
    fun synthesize(
        model: SpectralModel,
        sinusoidalGain: Float = 1.0f,
        residualGain: Float = 0.0f
    ): FloatArray {
        val baseSignal = STFT.synthesize(model.toSTFTAnalysis())

        if (abs(residualGain) < 1e-6f) {
            return if (abs(sinusoidalGain - 1f) < 1e-6f) {
                baseSignal
            } else {
                FloatArray(baseSignal.size) { i ->
                    (baseSignal[i] * sinusoidalGain).coerceIn(-1f, 1f)
                }
            }
        }

        val residualSignal = synthesizeResidual(model)
        return mix(
            tracks = listOf(baseSignal, residualSignal),
            gains = floatArrayOf(sinusoidalGain, residualGain),
            normalize = false
        )
    }

    private fun synthesizeResidual(model: SpectralModel): FloatArray {
        val n = model.windowSize
        val padding = n / 2
        val window = WindowFunctions.create(model.windowType, n)
        val outLen = model.originalLength + 2 * padding
        val output = DoubleArray(outLen)
        val norm = DoubleArray(outLen)

        for (frame in model.frames) {
            val start = frame.startSample + padding
            val res = frame.residual
            val limit = minOf(n, res.size)
            for (i in 0 until limit) {
                val idx = start + i
                if (idx !in output.indices) continue
                output[idx] += res[i] * window[i]
                norm[idx] += window[i] * window[i]
            }
        }

        return FloatArray(model.originalLength) { i ->
            val idx = i + padding
            val d = norm[idx]
            if (d > 1e-12) (output[idx] / d).toFloat() else 0f
        }
    }

    /**
     * Mixe plusieurs pistes audio mono avec gains individuels et clipping/normalisation optionnelle.
     */
    @JvmOverloads
    fun mix(
        tracks: List<FloatArray>,
        gains: FloatArray? = null,
        normalize: Boolean = false
    ): FloatArray {
        if (tracks.isEmpty()) return FloatArray(0)
        val maxLen = tracks.maxOf { it.size }
        val mixed = FloatArray(maxLen)

        for (t in tracks.indices) {
            val track = tracks[t]
            val gain = if (gains != null && t < gains.size) gains[t] else 1.0f
            for (i in track.indices) {
                mixed[i] += track[i] * gain
            }
        }

        if (normalize) {
            var peak = 0f
            for (v in mixed) {
                peak = max(peak, abs(v))
            }
            if (peak > 1e-6f) {
                val inv = 1f / peak
                for (i in mixed.indices) {
                    mixed[i] *= inv
                }
            }
        } else {
            for (i in mixed.indices) {
                mixed[i] = mixed[i].coerceIn(-1f, 1f)
            }
        }

        return mixed
    }

    /**
     * Intégration au multisampler :
     * Génère un SamplerInstrument multi-zones à partir d'un SampleData de base
     * en utilisant le Phase Vocoder (transposition à durée constante) et les
     * transformations spectrales (pente spectrale, formants).
     */
    @JvmOverloads
    fun buildMultisamplerInstrument(
        name: String,
        baseSample: SampleData,
        anchorMidiNotes: List<Int>,
        author: String = "Empty3 Fourier Engine",
        preserveFormants: Boolean = false,
        spectralTiltDb: Double = 0.0,
        formantShiftRatio: Double = 1.0,
        polyphony: Int = 32,
        envelope: AdsrSettings = AdsrSettings(),
        loop: LoopRegion? = null,
        windowSize: Int = 2048,
        hopSize: Int = 512
    ): SamplerInstrument {
        require(anchorMidiNotes.isNotEmpty()) { "anchorMidiNotes must not be empty" }

        val sortedNotes = anchorMidiNotes.distinct().sorted()
        val zones = ArrayList<SampleZone>(sortedNotes.size)

        for (idx in sortedNotes.indices) {
            val targetMidi = sortedNotes[idx]
            val semitones = (targetMidi - baseSample.rootMidi).toDouble()

            var processed = PhaseVocoder.pitchShiftSample(
                sample = baseSample,
                semitones = semitones,
                windowSize = windowSize,
                hopSize = hopSize,
                preserveFormants = preserveFormants
            )

            if (abs(spectralTiltDb) > 1e-6 || abs(formantShiftRatio - 1.0) > 1e-6) {
                processed = applyTimbreToSample(
                    sample = processed,
                    spectralTiltDb = spectralTiltDb,
                    formantShiftRatio = formantShiftRatio,
                    windowSize = windowSize,
                    hopSize = hopSize
                )
            }

            val keyLow = if (idx == 0) 0 else (sortedNotes[idx - 1] + targetMidi) / 2 + 1
            val keyHigh = if (idx == sortedNotes.lastIndex) 127 else (targetMidi + sortedNotes[idx + 1]) / 2

            val validLoop = loop?.takeIf { it.enabled && it.isValid(processed) }

            zones.add(
                SampleZone(
                    name = "$name-MIDI-$targetMidi",
                    sample = processed,
                    keyLow = keyLow.coerceIn(0, 127),
                    keyHigh = keyHigh.coerceIn(keyLow.coerceIn(0, 127), 127),
                    rootMidi = targetMidi,
                    envelope = envelope,
                    loop = validLoop
                )
            )
        }

        return SamplerInstrument(
            name = name,
            author = author,
            zones = zones,
            polyphony = polyphony
        )
    }

    private fun applyTimbreToSample(
        sample: SampleData,
        spectralTiltDb: Double,
        formantShiftRatio: Double,
        windowSize: Int,
        hopSize: Int
    ): SampleData {
        val frames = sample.frameCount
        val channels = sample.channels
        val outChannels = Array(channels) { ch ->
            val mono = FloatArray(frames) { f -> sample.getSample(f, ch) }
            val analysis = STFT.analyze(mono, sample.sampleRate, windowSize, hopSize)
            if (abs(spectralTiltDb) > 1e-6) {
                TimbreTransform.applySpectralTilt(analysis, spectralTiltDb)
            }
            if (abs(formantShiftRatio - 1.0) > 1e-6) {
                TimbreTransform.applyFormantShift(analysis, formantShiftRatio)
            }
            STFT.synthesize(analysis)
        }

        val interleaved = FloatArray(frames * channels)
        for (f in 0 until frames) {
            for (ch in 0 until channels) {
                interleaved[f * channels + ch] = outChannels[ch][f].coerceIn(-1f, 1f)
            }
        }

        return SampleData(
            samples = interleaved,
            sampleRate = sample.sampleRate,
            channels = channels,
            rootMidi = sample.rootMidi
        )
    }
}
