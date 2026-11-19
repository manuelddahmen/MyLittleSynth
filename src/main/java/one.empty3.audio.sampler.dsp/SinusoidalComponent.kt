package one.empty3.audio.sampler.dsp.model

data class SinusoidalComponent(
    val frequency: Double,
    val amplitude: Float,
    val phase: Double,
    val bin: Int = 0
)

data class SignalFrame(
    val components: List<SinusoidalComponent>,
    val residual: FloatArray,
    val sampleRate: Int,
    val startSample: Int = 0
)
