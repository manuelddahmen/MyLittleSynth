package one.empty3.audio.sampler.dsp.stft

import kotlin.math.PI
import kotlin.math.cos

enum class WindowType {
    HANN,
    HAMMING,
    BLACKMAN,
    BLACKMAN_HARRIS,
    RECTANGULAR
}

object WindowFunctions {

    fun create(type: WindowType, size: Int): DoubleArray {
        require(size >= 2) { "Window size must be >= 2" }

        return when (type) {
            WindowType.HANN -> DoubleArray(size) { i ->
                0.5 * (1.0 - cos(2.0 * PI * i / size))
            }

            WindowType.HAMMING -> DoubleArray(size) { i ->
                0.54 - 0.46 * cos(2.0 * PI * i / size)
            }

            WindowType.BLACKMAN -> DoubleArray(size) { i ->
                val x = 2.0 * PI * i / size
                0.42 - 0.5 * cos(x) + 0.08 * cos(2.0 * x)
            }

            WindowType.BLACKMAN_HARRIS -> DoubleArray(size) { i ->
                val x = 2.0 * PI * i / size
                0.35875 -
                    0.48829 * cos(x) +
                    0.14128 * cos(2.0 * x) -
                    0.01168 * cos(3.0 * x)
            }

            WindowType.RECTANGULAR -> DoubleArray(size) { 1.0 }
        }
    }
}
