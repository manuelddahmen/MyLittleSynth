package one.empty3.audio.sampler.dsp.stft

import one.empty3.audio.sampler.dsp.fft.Radix2FFT

data class STFTAnalysis(
    val sampleRate: Int,
    val originalLength: Int,
    val windowSize: Int,
    val hopSize: Int,
    val frames: List<SpectralFrame>,
    val windowType: WindowType = WindowType.HANN
)

object STFT {

    /**
     * Analyse un signal mono.
     */
    @JvmOverloads
    fun analyze(
        samples: FloatArray,
        sampleRate: Int,
        windowSize: Int = 2048,
        hopSize: Int = 512,
        windowType: WindowType = WindowType.HANN
    ): STFTAnalysis {

        require(sampleRate > 0)
        require(samples.isNotEmpty())
        require(
            windowSize >= 2 &&
                (windowSize and (windowSize - 1)) == 0
        )
        require(hopSize in 1 until windowSize)

        val window = WindowFunctions.create(windowType, windowSize)

        /*
         * Padding symétrique :
         * N/2 zéros au début et N/2 à la fin.
         *
         * Cela facilite la reconstruction des extrémités.
         */
        val padding = windowSize / 2

        val padded = DoubleArray(
            samples.size + windowSize
        )

        for (i in samples.indices) {
            padded[i + padding] = samples[i].toDouble()
        }

        val frames = ArrayList<SpectralFrame>()

        var start = 0

        while (start + windowSize <= padded.size) {

            val real = DoubleArray(windowSize)
            val imag = DoubleArray(windowSize)

            for (i in 0 until windowSize) {
                real[i] = padded[start + i] * window[i]
            }

            Radix2FFT.transform(real, imag)

            frames.add(
                SpectralFrame(
                    startSample = start - padding,
                    real = real,
                    imag = imag
                )
            )

            start += hopSize
        }

        return STFTAnalysis(
            sampleRate = sampleRate,
            originalLength = samples.size,
            windowSize = windowSize,
            hopSize = hopSize,
            frames = frames,
            windowType = windowType
        )
    }

    /**
     * Reconstruction mono par IFFT et Overlap-Add.
     *
     * Les coefficients spectraux ne sont pas modifiés
     * par cette opération.
     */
    fun synthesize(
        analysis: STFTAnalysis
    ): FloatArray {

        val n = analysis.windowSize
        val padding = n / 2

        val window = WindowFunctions.create(analysis.windowType, n)

        val outputLength =
            analysis.originalLength + 2 * padding

        val output = DoubleArray(outputLength)
        val normalization = DoubleArray(outputLength)

        for (frame in analysis.frames) {

            // Ne pas modifier les coefficients de l'analyse.
            val real = frame.real.copyOf()
            val imag = frame.imag.copyOf()

            Radix2FFT.transform(
                real,
                imag,
                inverse = true
            )

            val start = frame.startSample + padding

            for (i in 0 until n) {
                val index = start + i

                if (index !in output.indices) continue

                output[index] += real[i] * window[i]

                normalization[index] +=
                    window[i] * window[i]
            }
        }

        // Retrait du padding et normalisation.
        return FloatArray(analysis.originalLength) { i ->

            val index = i + padding
            val divisor = normalization[index]

            if (divisor > 1e-12) {
                (output[index] / divisor).toFloat()
            } else {
                0f
            }
        }
    }
}
