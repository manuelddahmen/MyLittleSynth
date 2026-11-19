
package one.empty3.audio.sampler.dsp.analysis

import one.empty3.audio.sampler.dsp.stft.SpectralFrame
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A sinusoidal component detected in one STFT frame.
 *
 * frequencyHz is refined by parabolic interpolation.
 * binIndex identifies the FFT bin containing the peak.
 *
 * spectralReal and spectralImag retain the original complex
 * coefficient at that FFT bin. They allow exact reconstruction
 * of the original spectrum when combined with the residual spectrum.
 *
 * amplitude is an approximate sinusoidal peak amplitude, assuming
 * the supplied windowSum corresponds to the analysis window.
 */
data class SinusoidalPartial(
    val binIndex: Int,
    val binPosition: Double,
    val frequencyHz: Double,
    val amplitude: Double,
    val phaseRadians: Double,
    val magnitude: Double,
    val spectralReal: Double,
    val spectralImag: Double
)

/**
 * Result of decomposing one STFT frame.
 *
 * residualReal/residualImag contain the original spectrum with
 * the selected peak-bin coefficients removed, including their
 * conjugate mirror bins.
 *
 * The decomposition is exact in the discrete FFT-bin domain:
 *
 * originalSpectrum = selectedPeakBins + residualSpectrum
 *
 * The oscillator interpretation of interpolated frequencies is
 * an approximation and is not itself an exact FFT-bin reconstruction.
 */
data class SinusoidalFrame(
    val startSample: Int,
    val sampleRate: Int,
    val fftSize: Int,
    val partials: List<SinusoidalPartial>,
    val residualReal: DoubleArray,
    val residualImag: DoubleArray
)

class SinusoidalDecomposer(
    private val relativeThresholdDb: Double = -60.0,
    private val minFrequencyHz: Double = 20.0,
    private val maxFrequencyHz: Double = Double.POSITIVE_INFINITY,
    private val minPeakDistanceBins: Int = 2,
    private val maxPartials: Int = 128
) {
    init {
        require(relativeThresholdDb <= 0.0)
        require(minFrequencyHz >= 0.0)
        require(maxFrequencyHz > minFrequencyHz)
        require(minPeakDistanceBins >= 1)
        require(maxPartials >= 1)
    }

    /**
     * Decomposes a single STFT frame.
     *
     * For the periodic Hann window used by the STFT implementation,
     * windowSum is approximately fftSize / 2.
     */
    fun decompose(
        frame: SpectralFrame,
        sampleRate: Int,
        windowSum: Double = frame.size / 2.0
    ): SinusoidalFrame {
        require(sampleRate > 0)
        require(frame.size >= 4)
        require(frame.real.size == frame.imag.size)
        require(windowSum > 0.0)

        val n = frame.size
        val lastPositiveBin = n / 2

        val magnitudes = DoubleArray(lastPositiveBin + 1) { k ->
            sqrt(
                frame.real[k] * frame.real[k] +
                frame.imag[k] * frame.imag[k]
            )
        }

        val maximumMagnitude = magnitudes.maxOrNull() ?: 0.0

        val threshold = maximumMagnitude *
            10.0.pow(relativeThresholdDb / 20.0)

        val candidates = mutableListOf<Candidate>()

        // DC and Nyquist are excluded from sinusoidal peak detection.
        for (k in 1 until lastPositiveBin) {
            val magnitude = magnitudes[k]

            if (magnitude < threshold) continue

            // A local maximum must be strictly greater than its
            // left neighbour and at least as large as its right one.
            if (magnitude <= magnitudes[k - 1]) continue
            if (magnitude < magnitudes[k + 1]) continue

            val delta = interpolatePeak(
                magnitudes[k - 1],
                magnitude,
                magnitudes[k + 1]
            )

            val binPosition = k + delta
            val frequencyHz =
                binPosition * sampleRate.toDouble() / n

            if (frequencyHz < minFrequencyHz ||
                frequencyHz > maxFrequencyHz
            ) continue

            val interpolatedMagnitude =
                interpolateMagnitude(
                    magnitudes[k - 1],
                    magnitude,
                    magnitudes[k + 1],
                    delta
                )

            candidates += Candidate(
                binIndex = k,
                binPosition = binPosition,
                frequencyHz = frequencyHz,
                magnitude = interpolatedMagnitude
            )
        }

        // Keep the strongest peaks first, enforcing a minimum
        // distance between selected FFT bins.
        val selected = mutableListOf<Candidate>()

        for (candidate in candidates.sortedByDescending { it.magnitude }) {
            if (selected.size >= maxPartials) break

            val tooClose = selected.any {
                abs(it.binIndex - candidate.binIndex) <
                    minPeakDistanceBins
            }

            if (!tooClose) {
                selected += candidate
            }
        }

        val partials = selected.map { candidate ->
            val k = candidate.binIndex
            val real = frame.real[k]
            val imag = frame.imag[k]

            val isNyquist = n % 2 == 0 && k == n / 2
            val isDc = k == 0

            val amplitude = when {
                isDc || isNyquist ->
                    candidate.magnitude / windowSum

                else ->
                    2.0 * candidate.magnitude / windowSum
            }

            SinusoidalPartial(
                binIndex = k,
                binPosition = candidate.binPosition,
                frequencyHz = candidate.frequencyHz,
                amplitude = amplitude,
                phaseRadians = atan2(imag, real),
                magnitude = candidate.magnitude,
                spectralReal = real,
                spectralImag = imag
            )
        }.sortedBy { it.frequencyHz }

        // Copy the complete complex spectrum.
        val residualReal = frame.real.copyOf()
        val residualImag = frame.imag.copyOf()

        // Remove selected positive-frequency bins and their
        // negative-frequency conjugate counterparts.
        for (partial in partials) {
            val k = partial.binIndex

            residualReal[k] = 0.0
            residualImag[k] = 0.0

            val mirror = (n - k) % n

            if (mirror != k) {
                residualReal[mirror] = 0.0
                residualImag[mirror] = 0.0
            }
        }

        return SinusoidalFrame(
            startSample = frame.startSample,
            sampleRate = sampleRate,
            fftSize = n,
            partials = partials,
            residualReal = residualReal,
            residualImag = residualImag
        )
    }

    /**
     * Parabolic interpolation of the logarithm of the magnitude.
     *
     * Returns a fractional-bin offset, normally between -0.5 and 0.5.
     */
    private fun interpolatePeak(
        left: Double,
        center: Double,
        right: Double
    ): Double {
        val epsilon = 1e-20

        val a = ln(max(left, epsilon))
        val b = ln(max(center, epsilon))
        val c = ln(max(right, epsilon))

        val denominator = a - 2.0 * b + c

        if (abs(denominator) < 1e-15) return 0.0

        val delta = 0.5 * (a - c) / denominator

        return delta.coerceIn(-0.5, 0.5)
    }

    /**
     * Estimates the magnitude at the interpolated peak position.
     * This is a local parabolic approximation.
     */
    private fun interpolateMagnitude(
        left: Double,
        center: Double,
        right: Double,
        delta: Double
    ): Double {
        val curvature = 0.5 * (left - right)
        val secondDifference = left - 2.0 * center + right

        val estimate =
            center + curvature * delta +
                0.5 * secondDifference * delta * delta

        return max(0.0, estimate)
    }

    private data class Candidate(
        val binIndex: Int,
        val binPosition: Double,
        val frequencyHz: Double,
        val magnitude: Double
    )
}
