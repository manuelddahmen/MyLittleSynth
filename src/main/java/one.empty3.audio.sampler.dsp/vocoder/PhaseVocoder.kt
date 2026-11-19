package one.empty3.audio.sampler.dsp.vocoder

import one.empty3.audio.sampler.core.SampleData
import one.empty3.audio.sampler.dsp.model.SpectralModel
import one.empty3.audio.sampler.dsp.stft.STFT
import one.empty3.audio.sampler.dsp.stft.STFTAnalysis
import one.empty3.audio.sampler.dsp.stft.SpectralFrame
import one.empty3.audio.sampler.dsp.stft.WindowType
import one.empty3.audio.sampler.dsp.transform.TimbreTransform
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 4. Phase Vocoder
 * Étirement temporel sans changement de hauteur et transposition indépendante de la durée
 * avec maintien de la continuité des phases.
 */
object PhaseVocoder {

    /**
     * Étire ou compresse la durée d'un signal mono sans modifier la hauteur.
     *
     * stretchFactor > 1.0 : plus long
     * stretchFactor < 1.0 : plus court
     */
    @JvmOverloads
    fun timeStretch(
        samples: FloatArray,
        sampleRate: Int,
        stretchFactor: Double,
        windowSize: Int = 2048,
        hopSize: Int = 512,
        windowType: WindowType = WindowType.HANN
    ): FloatArray {
        require(samples.isNotEmpty()) { "Input samples must not be empty" }
        require(stretchFactor > 0.0) { "stretchFactor must be > 0" }

        if (kotlin.math.abs(stretchFactor - 1.0) < 1e-6) {
            return samples.copyOf()
        }

        val analysis = STFT.analyze(
            samples = samples,
            sampleRate = sampleRate,
            windowSize = windowSize,
            hopSize = hopSize,
            windowType = windowType
        )

        if (analysis.frames.isEmpty()) {
            return samples.copyOf()
        }

        val n = windowSize
        val nyquist = n / 2
        val binCount = nyquist + 1
        val expectedAdvance = 2.0 * PI * hopSize / n

        val outputLength = (samples.size * stretchFactor)
            .roundToInt()
            .coerceAtLeast(1)

        val numOutFrames = ((analysis.frames.size - 1) * stretchFactor)
            .roundToInt()
            .coerceAtLeast(1) + 1

        val synthFrames = ArrayList<SpectralFrame>(numOutFrames)
        val synthPhases = DoubleArray(binCount) { k ->
            analysis.frames[0].phase(k)
        }

        for (outIdx in 0 until numOutFrames) {
            val pos = outIdx / stretchFactor
            val frameIdx0 = floor(pos).toInt().coerceIn(0, analysis.frames.lastIndex)
            val frameIdx1 = (frameIdx0 + 1).coerceAtMost(analysis.frames.lastIndex)
            val frac = (pos - frameIdx0).coerceIn(0.0, 1.0)

            val f0 = analysis.frames[frameIdx0]
            val f1 = analysis.frames[frameIdx1]

            val real = DoubleArray(n)
            val imag = DoubleArray(n)

            for (k in 0..nyquist) {
                val mag0 = f0.magnitude(k)
                val mag1 = f1.magnitude(k)
                val mag = mag0 * (1.0 - frac) + mag1 * frac

                if (outIdx > 0) {
                    val phase0 = f0.phase(k)
                    val phase1 = f1.phase(k)
                    val expected = k * expectedAdvance
                    val delta = SpectralModel.wrapPhase(phase1 - phase0 - expected)
                    val trueAdvance = expected + delta
                    synthPhases[k] = SpectralModel.wrapPhase(synthPhases[k] + trueAdvance)
                }

                val ph = synthPhases[k]
                real[k] = mag * cos(ph)
                imag[k] = mag * sin(ph)

                if (k > 0 && k < nyquist) {
                    val mirror = n - k
                    real[mirror] = real[k]
                    imag[mirror] = -imag[k]
                }
            }

            synthFrames.add(
                SpectralFrame(
                    startSample = outIdx * hopSize - n / 2,
                    real = real,
                    imag = imag
                )
            )
        }

        val synthAnalysis = STFTAnalysis(
            sampleRate = sampleRate,
            originalLength = outputLength,
            windowSize = windowSize,
            hopSize = hopSize,
            frames = synthFrames,
            windowType = windowType
        )

        return STFT.synthesize(synthAnalysis)
    }

    /**
     * Transpose un signal mono en conservant exactement sa durée originale.
     *
     * semitones > 0 : plus aigu
     * semitones < 0 : plus grave
     */
    @JvmOverloads
    fun pitchShift(
        samples: FloatArray,
        sampleRate: Int,
        semitones: Double,
        windowSize: Int = 2048,
        hopSize: Int = 512,
        windowType: WindowType = WindowType.HANN,
        preserveFormants: Boolean = false
    ): FloatArray {
        require(samples.isNotEmpty()) { "Input samples must not be empty" }
        if (kotlin.math.abs(semitones) < 1e-6) {
            return samples.copyOf()
        }

        val pitchRatio = 2.0.pow(semitones / 12.0)

        // Étape 1 : étirement temporel par pitchRatio (conserve la hauteur)
        val stretched = timeStretch(
            samples = samples,
            sampleRate = sampleRate,
            stretchFactor = pitchRatio,
            windowSize = windowSize,
            hopSize = hopSize,
            windowType = windowType
        )

        // Étape 2 : rééchantillonnage vers la durée originale (modifie la hauteur)
        val shifted = resampleToLength(stretched, samples.size)

        if (!preserveFormants) {
            return shifted
        }

        // Étape 3 optionnelle : compensation du décalage de formants
        val analysis = STFT.analyze(shifted, sampleRate, windowSize, hopSize, windowType)
        TimbreTransform.applyFormantShift(analysis, 1.0 / pitchRatio)
        return STFT.synthesize(analysis)
    }

    /**
     * Transpose un SampleData (mono ou stéréo) sans modifier sa durée.
     */
    @JvmOverloads
    fun pitchShiftSample(
        sample: SampleData,
        semitones: Double,
        windowSize: Int = 2048,
        hopSize: Int = 512,
        preserveFormants: Boolean = false
    ): SampleData {
        val frames = sample.frameCount
        val channels = sample.channels
        val shiftedChannels = Array(channels) { ch ->
            val mono = FloatArray(frames) { f -> sample.getSample(f, ch) }
            pitchShift(
                samples = mono,
                sampleRate = sample.sampleRate,
                semitones = semitones,
                windowSize = windowSize,
                hopSize = hopSize,
                preserveFormants = preserveFormants
            )
        }

        val outSamples = FloatArray(frames * channels)
        for (f in 0 until frames) {
            for (ch in 0 until channels) {
                outSamples[f * channels + ch] = shiftedChannels[ch][f].coerceIn(-1f, 1f)
            }
        }

        val newRootMidi = (sample.rootMidi + semitones.roundToInt()).coerceIn(0, 127)
        return SampleData(
            samples = outSamples,
            sampleRate = sample.sampleRate,
            channels = channels,
            rootMidi = newRootMidi
        )
    }

    private fun resampleToLength(input: FloatArray, targetLength: Int): FloatArray {
        if (input.size == targetLength) return input.copyOf()
        if (targetLength <= 1) return floatArrayOf(input[0])

        val scale = (input.size - 1).toDouble() / (targetLength - 1).toDouble()
        return FloatArray(targetLength) { i ->
            val pos = i * scale
            val i0 = floor(pos).toInt().coerceIn(0, input.lastIndex)
            val i1 = (i0 + 1).coerceAtMost(input.lastIndex)
            val frac = (pos - i0).toFloat()
            input[i0] * (1f - frac) + input[i1] * frac
        }
    }
}
