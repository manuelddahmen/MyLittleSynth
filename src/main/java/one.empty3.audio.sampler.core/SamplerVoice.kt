package one.empty3.audio.sampler.core

import kotlin.math.pow

class SamplerVoice(
    private val outputSampleRate: Int
) {
    private var sample: SampleData? = null

    private var position = 0.0
    private var increment = 1.0

    private var loop: LoopRegion? = null
    private var looping = false

    var midiNote = -1
        private set

    var velocity = 0f
        private set

    var volume = 1f

    var age = 0L
        private set

    private var ageCounter = 0L

    val envelope = AdsrEnvelope()

    val isActive: Boolean
        get() = midiNote >= 0 && envelope.isActive

    init {
        require(outputSampleRate > 0)
        envelope.setSampleRate(outputSampleRate)
    }

    fun playNote(
        sample: SampleData,
        targetMidi: Int,
        velocity: Float = 1f,
        loop: LoopRegion? = null
    ) {
        if (sample.frameCount < 2) return

        this.sample = sample
        this.midiNote = targetMidi
        this.velocity = velocity.coerceIn(0f, 1f)

        this.position = 0.0

        // MIDI pitch transposition + sample-rate conversion.
        val semitones = targetMidi - sample.rootMidi

        val pitchRatio = 2.0.pow(semitones / 12.0)

        increment = pitchRatio *
                sample.sampleRate.toDouble() /
                outputSampleRate.toDouble()

        this.loop = loop?.takeIf { it.enabled && it.isValid(sample) }

        looping = this.loop != null

        age = ++ageCounter

        envelope.noteOn()
    }

    fun noteOff() {
        if (isActive) {
            // The sample leaves its sustain loop on release.
            looping = false
            envelope.noteOff()
        }
    }

    fun stop() {
        envelope.noteOff()
        midiNote = -1
        sample = null
        looping = false
    }

    fun render(
        output: FloatArray,
        outputOffset: Int
    ) {
        if (!isActive) return

        val source = sample ?: return

        val region = loop

        if (looping && region != null &&
            position >= region.endFrame
        ) {
            position = region.startFrame.toDouble() +
                    (position - region.startFrame) %
                    (region.endFrame - region.startFrame)
        }

        val index0 = position.toInt()

        if (index0 < 0 || index0 >= source.frameCount) {
            stop()
            return
        }

        var index1 = index0 + 1

        if (looping && region != null &&
            index1 >= region.endFrame
        ) {
            index1 = region.startFrame
        } else if (index1 >= source.frameCount) {
            index1 = source.frameCount - 1
        }

        val fraction = (position - index0).toFloat()

        val left0 = source.getSample(index0, 0)
        val left1 = source.getSample(index1, 0)

        val right0 = source.getSample(index0, 1)
        val right1 = source.getSample(index1, 1)

        val left = left0 + fraction * (left1 - left0)
        val right = right0 + fraction * (right1 - right0)

        val gain = envelope.nextLevel() * velocity * volume

        output[outputOffset] += left * gain
        output[outputOffset + 1] += right * gain

        position += increment

        if (!looping && position >= source.frameCount) {
            stop()
        }

        if (!envelope.isActive) {
            stop()
        }
    }
}
