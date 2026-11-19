
package one.empty3.audio.sampler.core

data class SamplerInstrument(
    val name: String,
    val author: String = "",
    val zones: List<SampleZone>,
    val polyphony: Int = 32,
    val masterVolume: Float = 0.8f
) {
    init {
        require(name.isNotBlank())
        require(zones.isNotEmpty())
        require(polyphony in 1..256)
        require(masterVolume >= 0f)
    }

    /**
     * Select the most specific matching zone.
     *
     * This permits velocity layers and key ranges
     * without relying on the order of the JSON array.
     */
    fun findZone(note: Int, velocity: Int): SampleZone? {
        return zones
            .filter { it.matches(note, velocity) }
            .minWithOrNull(
                compareBy<SampleZone> {
                    it.keyHigh - it.keyLow
                }.thenBy {
                    it.velocityHigh - it.velocityLow
                }
            )
    }
}
