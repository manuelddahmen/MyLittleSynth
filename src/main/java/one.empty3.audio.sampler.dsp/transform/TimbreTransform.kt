package one.empty3.audio.sampler.dsp.transform

import one.empty3.audio.sampler.dsp.stft.STFTAnalysis
import one.empty3.audio.sampler.dsp.stft.SpectralFrame
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

object TimbreTransform {

    /**
     * Modifie la pente spectrale du signal.
     *
     * dbPerOctave :
     *   0   = aucun changement
     *   6   = renforcement de 6 dB par octave
     *  -6   = atténuation de 6 dB par octave
     *
     * referenceHz :
     *   fréquence à laquelle le gain vaut 0 dB.
     */
    @JvmOverloads
    fun applySpectralTilt(
        analysis: STFTAnalysis,
        dbPerOctave: Double,
        referenceHz: Double = 1000.0
    ) {
        require(referenceHz > 0.0)

        val n = analysis.windowSize
        val nyquist = n / 2

        for (frame in analysis.frames) {

            for (k in 0..nyquist) {

                val frequency =
                    k.toDouble() * analysis.sampleRate / n

                val gainDb = if (frequency > 0.0) {
                    dbPerOctave *
                        log2(frequency / referenceHz)
                } else {
                    0.0
                }

                val gain = 10.0.pow(gainDb / 20.0)

                frame.real[k] *= gain
                frame.imag[k] *= gain

                // Symétrie conjuguée pour le signal réel.
                if (k > 0 && k < nyquist) {
                    val mirror = n - k

                    frame.real[mirror] *= gain
                    frame.imag[mirror] *= gain
                }
            }
        }
    }

    /**
     * Estime l'enveloppe spectrale lisse (log-magnitude lissée) d'une trame.
     */
    @JvmOverloads
    fun estimateSpectralEnvelope(
        magnitudes: DoubleArray,
        smoothingRadius: Int = 8
    ): DoubleArray {
        val len = magnitudes.size
        val radius = smoothingRadius.coerceAtLeast(1)
        val logMags = DoubleArray(len) { k -> ln(max(magnitudes[k], 1e-12)) }
        val env = DoubleArray(len)

        for (k in 0 until len) {
            val lo = max(0, k - radius)
            val hi = min(len - 1, k + radius)
            var sum = 0.0
            for (j in lo..hi) {
                sum += logMags[j]
            }
            env[k] = exp(sum / (hi - lo + 1))
        }
        return env
    }

    /**
     * Déplace les formants (enveloppe spectrale) sans modifier la hauteur fondamentale.
     *
     * shiftRatio > 1.0 : formants plus aigus / brillants.
     * shiftRatio < 1.0 : formants plus graves / sombres.
     */
    @JvmOverloads
    fun applyFormantShift(
        analysis: STFTAnalysis,
        shiftRatio: Double,
        smoothingRadius: Int = 8
    ) {
        require(shiftRatio > 0.0) { "shiftRatio must be > 0" }
        if (kotlin.math.abs(shiftRatio - 1.0) < 1e-6) return

        val n = analysis.windowSize
        val nyquist = n / 2

        for (frame in analysis.frames) {
            val mags = DoubleArray(nyquist + 1) { k -> frame.magnitude(k) }
            val env = estimateSpectralEnvelope(mags, smoothingRadius)

            for (k in 0..nyquist) {
                val srcPos = k / shiftRatio
                val shiftedEnv = if (srcPos >= 0.0 && srcPos <= nyquist) {
                    val i0 = srcPos.toInt().coerceIn(0, nyquist)
                    val i1 = (i0 + 1).coerceAtMost(nyquist)
                    val frac = srcPos - i0
                    env[i0] * (1.0 - frac) + env[i1] * frac
                } else {
                    1e-12
                }

                val ratio = if (env[k] > 1e-12) {
                    (shiftedEnv / env[k]).coerceIn(0.05, 20.0)
                } else {
                    1.0
                }

                frame.real[k] *= ratio
                frame.imag[k] *= ratio

                if (k > 0 && k < nyquist) {
                    val mirror = n - k
                    frame.real[mirror] *= ratio
                    frame.imag[mirror] *= ratio
                }
            }
        }
    }

    /**
     * Filtre spectral passe-bande / passe-bas / passe-haut.
     */
    @JvmOverloads
    fun applyBandFilter(
        analysis: STFTAnalysis,
        lowCutHz: Double = 0.0,
        highCutHz: Double = Double.POSITIVE_INFINITY
    ) {
        val n = analysis.windowSize
        val nyquist = n / 2

        for (frame in analysis.frames) {
            for (k in 0..nyquist) {
                val freq = k.toDouble() * analysis.sampleRate / n
                if (freq < lowCutHz || freq > highCutHz) {
                    frame.real[k] = 0.0
                    frame.imag[k] = 0.0
                    if (k > 0 && k < nyquist) {
                        val mirror = n - k
                        frame.real[mirror] = 0.0
                        frame.imag[mirror] = 0.0
                    }
                }
            }
        }
    }

    /**
     * Morphing spectral / création de timbre hybride entre deux analyses STFT.
     *
     * mix = 0.0 -> 100% analysisA
     * mix = 1.0 -> 100% analysisB
     */
    fun morphTimbre(
        analysisA: STFTAnalysis,
        analysisB: STFTAnalysis,
        mix: Double
    ): STFTAnalysis {
        require(analysisA.windowSize == analysisB.windowSize) {
            "Both STFT analyses must have the same windowSize"
        }
        val m = mix.coerceIn(0.0, 1.0)
        val n = analysisA.windowSize
        val nyquist = n / 2
        val frameCount = min(analysisA.frames.size, analysisB.frames.size)
        val morphedFrames = ArrayList<SpectralFrame>(frameCount)

        for (f in 0 until frameCount) {
            val fa = analysisA.frames[f]
            val fb = analysisB.frames[f]
            val real = DoubleArray(n)
            val imag = DoubleArray(n)

            for (k in 0..nyquist) {
                val mag = fa.magnitude(k) * (1.0 - m) + fb.magnitude(k) * m
                val phase = if (m < 0.5) fa.phase(k) else fb.phase(k)
                real[k] = mag * cos(phase)
                imag[k] = mag * sin(phase)

                if (k > 0 && k < nyquist) {
                    val mirror = n - k
                    real[mirror] = real[k]
                    imag[mirror] = -imag[k]
                }
            }

            morphedFrames.add(
                SpectralFrame(
                    startSample = fa.startSample,
                    real = real,
                    imag = imag
                )
            )
        }

        return analysisA.copy(
            originalLength = min(analysisA.originalLength, analysisB.originalLength),
            frames = morphedFrames
        )
    }
}
