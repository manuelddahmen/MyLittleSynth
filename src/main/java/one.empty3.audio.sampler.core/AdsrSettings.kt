
package one.empty3.audio.sampler.core

data class AdsrSettings(
    val attack: Float = 0.01f,
    val decay: Float = 0.15f,
    val sustain: Float = 0.8f,
    val release: Float = 0.3f
) {
    init {
        require(attack >= 0f)
        require(decay >= 0f)
        require(sustain in 0f..1f)
        require(release >= 0f)
    }
}
