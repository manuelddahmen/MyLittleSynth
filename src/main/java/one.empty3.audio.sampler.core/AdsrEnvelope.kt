
package one.empty3.audio.sampler.core

class AdsrEnvelope(
    var attack: Float = 0.01f,
    var decay: Float = 0.15f,
    var sustain: Float = 0.75f,
    var release: Float = 0.25f
) {
    enum class Stage {
        IDLE, ATTACK, DECAY, SUSTAIN, RELEASE
    }

    var stage = Stage.IDLE
        private set

    var level = 0.0f
        private set

    private var sampleRate = 44100
    private var releaseStep = 0.0f

    fun setSampleRate(rate: Int) {
        require(rate > 0)
        sampleRate = rate
    }

   fun noteOn() {
    level = if (attack <= 0f) 1f else 0f

    stage = when {
        attack > 0f -> Stage.ATTACK
        decay > 0f -> Stage.DECAY
        else -> Stage.SUSTAIN
    }

    if (stage == Stage.SUSTAIN) {
        level = sustain.coerceIn(0f, 1f)
    }
}
    fun noteOff() {
        if (stage == Stage.IDLE) return

        releaseStep = if (release <= 0f)
            Float.POSITIVE_INFINITY
        else level / (release * sampleRate)

        stage = Stage.RELEASE

        if (release <= 0f) {
            level = 0f
            stage = Stage.IDLE
        }
    }

    fun nextLevel(): Float {
        when (stage) {
            Stage.IDLE -> {
                level = 0f
            }

            Stage.ATTACK -> {
                level += 1f / (attack * sampleRate)

                if (level >= 1f) {
                    level = 1f
                    stage = if (decay > 0f)
                        Stage.DECAY else Stage.SUSTAIN
                }
            }

       Stage.DECAY -> {
    val target = sustain.coerceIn(0f, 1f)

    if (decay <= 0f) {
        level = target
        stage = Stage.SUSTAIN
    } else {
        level -= (1f - target) /
                (decay * sampleRate)

        if (level <= target) {
            level = target
            stage = Stage.SUSTAIN
        }
    }
}

            Stage.SUSTAIN -> {
                level = sustain.coerceIn(0f, 1f)
            }

            Stage.RELEASE -> {
                level -= releaseStep

                if (level <= 0f) {
                    level = 0f
                    stage = Stage.IDLE
                }
            }
        }

        return level
    }

    val isActive: Boolean
        get() = stage != Stage.IDLE
}
