package one.empty3.audio.sampler.core

class SampleData @JvmOverloads constructor(
    val samples: FloatArray,
    val sampleRate: Int,
    val channels: Int,
    val rootMidi: Int = 60
) {
    init {
        require(sampleRate > 0)
        require(channels == 1 || channels == 2)
        require(samples.size % channels == 0)
    }

    val frameCount: Int
        get() = samples.size / channels

    fun getSample(frame: Int, channel: Int): Float {
        if (frame < 0 || frame >= frameCount) return 0f
        val actualChannel = if (channels == 1) 0 else channel.coerceIn(0, channels - 1)
        return samples[frame * channels + actualChannel]
    }
}
