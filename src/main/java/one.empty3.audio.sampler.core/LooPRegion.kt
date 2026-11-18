package one.empty3.apps.mylittlesynth

data class LoopRegion(
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
