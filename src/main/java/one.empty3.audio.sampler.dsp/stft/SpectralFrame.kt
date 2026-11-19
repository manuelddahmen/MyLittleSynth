package one.empty3.audio.sampler.dsp.stft

data class SpectralFrame(
    val startSample: Int,
    val real: DoubleArray,
    val imag: DoubleArray
) {
    val size: Int
        get() = real.size

    fun magnitude(bin: Int): Double {
        return kotlin.math.hypot(
            real[bin],
            imag[bin]
        )
    }

    fun phase(bin: Int): Double {
        return kotlin.math.atan2(
            imag[bin],
            real[bin]
        )
    }

    fun frequency(
        bin: Int,
        sampleRate: Int
    ): Double {
        return bin.toDouble() * sampleRate / size
    }
}