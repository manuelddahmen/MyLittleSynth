package one.empty3.audio.sampler.dsp.fft

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object Radix2FFT {

    /**
     * Transformée de Fourier rapide.
     *
     * inverse = false : FFT
     * inverse = true  : IFFT
     *
     * Les tableaux real et imag doivent avoir :
     * - la même taille ;
     * - une taille qui est une puissance de 2.
     *
     * Les résultats sont écrits directement dans les tableaux.
     */
    fun transform(
        real: DoubleArray,
        imag: DoubleArray,
        inverse: Boolean = false
    ) {
        val n = real.size

        require(n == imag.size) {
            "Real and imaginary arrays must have the same size"
        }

        require(n >= 2 && (n and (n - 1)) == 0) {
            "FFT size must be a power of two >= 2"
        }

        // Réorganisation bit-reversal.
        var j = 0

        for (i in 1 until n) {
            var bit = n shr 1

            while ((j and bit) != 0) {
                j = j xor bit
                bit = bit shr 1
            }

            j = j xor bit

            if (i < j) {
                var temp = real[i]
                real[i] = real[j]
                real[j] = temp

                temp = imag[i]
                imag[i] = imag[j]
                imag[j] = temp
            }
        }

        // Étapes successives de papillon.
        var length = 2

        while (length <= n) {
            val angle = (if (inverse) 2.0 else -2.0) *
                    PI / length

            val wLenReal = cos(angle)
            val wLenImag = sin(angle)

            val half = length / 2

            var start = 0

            while (start < n) {
                var wReal = 1.0
                var wImag = 0.0

                for (k in 0 until half) {
                    val even = start + k
                    val odd = even + half

                    val oddReal =
                        real[odd] * wReal -
                        imag[odd] * wImag

                    val oddImag =
                        real[odd] * wImag +
                        imag[odd] * wReal

                    val evenReal = real[even]
                    val evenImag = imag[even]

                    real[even] = evenReal + oddReal
                    imag[even] = evenImag + oddImag

                    real[odd] = evenReal - oddReal
                    imag[odd] = evenImag - oddImag

                    val nextWReal =
                        wReal * wLenReal -
                        wImag * wLenImag

                    wImag =
                        wReal * wLenImag +
                        wImag * wLenReal

                    wReal = nextWReal
                }

                start += length
            }

            if (length == n) break
            length *= 2
        }

        // Normalisation de l'IFFT.
        if (inverse) {
            val scale = 1.0 / n

            for (i in 0 until n) {
                real[i] *= scale
                imag[i] *= scale
            }
        }
    }
}