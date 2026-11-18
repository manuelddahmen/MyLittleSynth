package one.empty3.audio.sampler.core

data class LoopRegion @JvmOverloads constructor(
    val startFrame: Int,
    val endFrame: Int,
    val enabled: Boolean = true
) {
    fun isValid(sample: SampleData): Boolean {
        return startFrame >= 0 &&
                endFrame <= sample.frameCount &&
                endFrame - startFrame >= 2
    }
}
