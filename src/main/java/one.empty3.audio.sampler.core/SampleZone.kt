
package one.empty3.audio.sampler.core

data class SampleZone(
    val name: String,
    val sample: SampleData,

    val keyLow: Int = 0,
    val keyHigh: Int = 127,

    val velocityLow: Int = 1,
    val velocityHigh: Int = 127,

    val rootMidi: Int = sample.rootMidi,

    val envelope: AdsrSettings = AdsrSettings(),

    val loop: LoopRegion? = null,

    val volume: Float = 1.0f
) {
    init {
        require(keyLow in 0..127)
        require(keyHigh in keyLow..127)

        require(velocityLow in 1..127)
        require(velocityHigh in velocityLow..127)

        require(rootMidi in 0..127)
        require(volume >= 0f)

        require(loop == null || loop.isValid(sample))
    }

    fun matches(note: Int, velocity: Int): Boolean {
        return note in keyLow..keyHigh &&
                velocity in velocityLow..velocityHigh
    }
}
