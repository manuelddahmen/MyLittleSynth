package one.empty3.audio.sampler.dsp.analyzer

import one.empty3.audio.sampler.dsp.fft.Radix2FFT
import one.empty3.audio.sampler.dsp.model.SinusoidalComponent
import one.empty3.audio.sampler.dsp.model.SpectralModel
import one.empty3.audio.sampler.dsp.model.SpectralModelFrame
import one.empty3.audio.sampler.dsp.stft.STFT
import one.empty3.audio.sampler.dsp.stft.STFTAnalysis
import one.empty3.audio.sampler.dsp.stft.SpectralFrame
import one.empty3.audio.sampler.dsp.stft.WindowType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max

/**
 * 1. Signal Analyzer
 * Analyse FFT/STFT, détection de pics sinusoïdaux avec interpolation parabolique,
 * calcul des fréquences instantanées et extraction du résidu stochastique.
 */
object SignalAnalyzer {

    @JvmStatic
    @JvmOverloads
    fun analyze(
        samples: FloatArray,
        sampleRate: Int,
        windowSize: Int = 2048,
        hopSize: Int = 512,
        windowType: WindowType = WindowType.HANN,
        maxComponents: Int = 32,
        thresholdDb: Double = -60.0
    ): SpectralModel {
        val stft = STFT.analyze(
            samples = samples,
            sampleRate = sampleRate,
            windowSize = windowSize,
            hopSize = hopSize,
            windowType = windowType
        )
        return buildSpectralModel(stft, maxComponents, thresholdDb)
    }

    @JvmOverloads
    fun buildSpectralModel(
        analysis: STFTAnalysis,
        maxComponents: Int = 32,
        thresholdDb: Double = -60.0
    ): SpectralModel {
        val n = analysis.windowSize
        val nyquist = n / 2
        val binCount = nyquist + 1
        val hop = analysis.hopSize
        val expectedAdvancePerBin = 2.0 * PI * hop / n

        val modelFrames = ArrayList<SpectralModelFrame>(analysis.frames.size)
        var prevPhases: DoubleArray? = null

        for (frame in analysis.frames) {
            val amplitudes = DoubleArray(binCount)
            val phases = DoubleArray(binCount)
            val instFreqs = DoubleArray(binCount)

            for (k in 0..nyquist) {
                amplitudes[k] = frame.magnitude(k)
                phases[k] = frame.phase(k)

                val prev = prevPhases
                if (prev == null) {
                    instFreqs[k] = frame.frequency(k, analysis.sampleRate)
                } else {
                    val expected = k * expectedAdvancePerBin
                    val deltaPhase = SpectralModel.wrapPhase(
                        phases[k] - prev[k] - expected
                    )
                    val trueBinFreq = k.toDouble() + deltaPhase * n / (2.0 * PI * hop)
                    instFreqs[k] = trueBinFreq * analysis.sampleRate / n
                }
            }

            val components = detectComponents(
                frame = frame,
                sampleRate = analysis.sampleRate,
                maxComponents = maxComponents,
                thresholdDb = thresholdDb
            )

            val residual = computeResidual(
                frame = frame,
                components = components
            )

            modelFrames.add(
                SpectralModelFrame(
                    startSample = frame.startSample,
                    amplitudes = amplitudes,
                    phases = phases,
                    instantaneousFrequencies = instFreqs,
                    components = components,
                    residual = residual
                )
            )
            prevPhases = phases
        }

        return SpectralModel(
            sampleRate = analysis.sampleRate,
            originalLength = analysis.originalLength,
            windowSize = analysis.windowSize,
            hopSize = analysis.hopSize,
            windowType = analysis.windowType,
            frames = modelFrames
        )
    }

    /**
     * Détecte les composantes sinusoïdales dominantes d'une trame spectrale
     * avec interpolation parabolique en log-magnitude.
     */
    @JvmOverloads
    fun detectComponents(
        frame: SpectralFrame,
        sampleRate: Int,
        maxComponents: Int = 32,
        thresholdDb: Double = -60.0
    ): List<SinusoidalComponent> {
        val n = frame.size
        val nyquist = n / 2
        val magnitudes = DoubleArray(nyquist + 1) { k -> frame.magnitude(k) }

        var maxMag = 0.0
        for (m in magnitudes) {
            if (m > maxMag) maxMag = m
        }
        if (maxMag <= 1e-12) return emptyList()

        val peaks = ArrayList<SinusoidalComponent>()

        for (k in 1 until nyquist) {
            val mPrev = magnitudes[k - 1]
            val mCurr = magnitudes[k]
            val mNext = magnitudes[k + 1]

            if (mCurr > mPrev && mCurr >= mNext && mCurr > 1e-12) {
                val db = 20.0 * log10(mCurr / maxMag)
                if (db < thresholdDb) continue

                val alpha = ln(max(mPrev, 1e-12))
                val beta = ln(max(mCurr, 1e-12))
                val gamma = ln(max(mNext, 1e-12))
                val denom = alpha - 2.0 * beta + gamma
                val delta = if (abs(denom) > 1e-12) {
                    (0.5 * (alpha - gamma) / denom).coerceIn(-0.5, 0.5)
                } else {
                    0.0
                }

                val refinedBin = k + delta
                val frequency = refinedBin * sampleRate / n
                val normalizedAmp = (2.0 * mCurr / n).toFloat()

                peaks.add(
                    SinusoidalComponent(
                        frequency = frequency,
                        amplitude = normalizedAmp,
                        phase = frame.phase(k),
                        bin = k
                    )
                )
            }
        }

        return peaks
            .sortedByDescending { it.amplitude }
            .take(maxComponents)
    }

    /**
     * Calcule la trame résiduelle (partie non-sinusoïdale / bruit / transitoires)
     * en retirant les bins autour des pics sinusoïdaux détectés puis IFFT.
     */
    private fun computeResidual(
        frame: SpectralFrame,
        components: List<SinusoidalComponent>
    ): FloatArray {
        val n = frame.size
        val nyquist = n / 2
        val resReal = frame.real.copyOf()
        val resImag = frame.imag.copyOf()

        for (comp in components) {
            val center = comp.bin
            for (offset in -1..1) {
                val k = center + offset
                if (k in 0..nyquist) {
                    resReal[k] = 0.0
                    resImag[k] = 0.0
                    if (k > 0 && k < nyquist) {
                        val mirror = n - k
                        resReal[mirror] = 0.0
                        resImag[mirror] = 0.0
                    }
                }
            }
        }

        Radix2FFT.transform(resReal, resImag, inverse = true)
        return FloatArray(n) { i -> resReal[i].toFloat() }
    }
}
