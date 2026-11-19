package one.empty3.audio.sampler.dsp.transform

import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

object PitchTransform {

    /**
     * Transposition simple par rééchantillonnage linéaire.
     *
     * semitones > 0 : son plus aigu et plus court.
     * semitones < 0 : son plus grave et plus long.
     *
     * Cette méthode ne conserve pas la durée.
     */
    fun transposeResampling(
        input: FloatArray,
        semitones: Double
    ): FloatArray {

        require(input.isNotEmpty())

        val ratio = 2.0.pow(semitones / 12.0)

        val outputLength =
            (input.size / ratio).roundToInt()
                .coerceAtLeast(1)

        val output = FloatArray(outputLength)

        for (i in output.indices) {

            val sourcePosition = i * ratio
            val index0 = floor(sourcePosition).toInt()

            val index1 =
                (index0 + 1).coerceAtMost(input.lastIndex)

            val fraction =
                (sourcePosition - index0).toFloat()

            output[i] =
                input[index0] * (1f - fraction) +
                input[index1] * fraction
        }

        return output
    }
}