package one.empty3.audio.sampler.dsp.model

import one.empty3.audio.sampler.dsp.stft.STFTAnalysis
import one.empty3.audio.sampler.dsp.stft.SpectralFrame
import one.empty3.audio.sampler.dsp.stft.WindowType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Représentation spectrale d'une trame :
 * amplitudes, phases, fréquences instantanées, composantes sinusoïdales et résidu.
 */
data class SpectralModelFrame(
    val startSample: Int,
    val amplitudes: DoubleArray,
    val phases: DoubleArray,
    val instantaneousFrequencies: DoubleArray,
    val components: List<SinusoidalComponent>,
    val residual: FloatArray
) {
    val binCount: Int
        get() = amplitudes.size

    fun toSpectralFrame(windowSize: Int): SpectralFrame {
        val nyquist = windowSize / 2
        val real = DoubleArray(windowSize)
        val imag = DoubleArray(windowSize)

        for (k in 0..nyquist) {
            val mag = amplitudes[k]
            val ph = phases[k]
            real[k] = mag * cos(ph)
            imag[k] = mag * sin(ph)

            if (k > 0 && k < nyquist) {
                val mirror = windowSize - k
                real[mirror] = real[k]
                imag[mirror] = -imag[k]
            }
        }

        return SpectralFrame(
            startSample = startSample,
            real = real,
            imag = imag
        )
    }
}

/**
 * Modèle spectral complet d'un signal analysé par STFT.
 */
data class SpectralModel(
    val sampleRate: Int,
    val originalLength: Int,
    val windowSize: Int,
    val hopSize: Int,
    val windowType: WindowType,
    val frames: List<SpectralModelFrame>
) {
    fun toSTFTAnalysis(): STFTAnalysis {
        return STFTAnalysis(
            sampleRate = sampleRate,
            originalLength = originalLength,
            windowSize = windowSize,
            hopSize = hopSize,
            frames = frames.map { it.toSpectralFrame(windowSize) },
            windowType = windowType
        )
    }

    companion object {
        fun wrapPhase(phase: Double): Double {
            var p = (phase + PI) % (2.0 * PI)
            if (p < 0.0) p += 2.0 * PI
            return p - PI
        }
    }
}
