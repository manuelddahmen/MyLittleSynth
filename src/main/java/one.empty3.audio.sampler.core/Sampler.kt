
package one.empty3.audio.sampler.core

import java.util.concurrent.ConcurrentLinkedQueue

class Sampler(
    private val sample: SampleData,
    private val outputSampleRate: Int = 44100,
    polyphony: Int = 16
) {
    private val voices = Array(polyphony) {
        SamplerVoice(outputSampleRate)
    }

    private sealed interface Command {
        data class NoteOn(
            val note: Int,
            val velocity: Float
        ) : Command

        data class NoteOff(
            val note: Int
        ) : Command

        data object AllNotesOff : Command
    }

    private val commands =
        ConcurrentLinkedQueue<Command>()

    var masterVolume = 0.8f

    var loopRegion: LoopRegion? = null

    fun noteOn(note: Int, velocity: Float = 1f) {
        commands.add(Command.NoteOn(note, velocity))
    }

    fun noteOff(note: Int) {
        commands.add(Command.NoteOff(note))
    }

    fun allNotesOff() {
        commands.add(Command.AllNotesOff)
    }

    private fun allocateVoice(): SamplerVoice {
        for (voice in voices) {
            if (!voice.isActive) return voice
        }

        // Voice stealing: oldest active voice.
        var oldest = voices[0]

        for (voice in voices) {
            if (voice.age < oldest.age) {
                oldest = voice
            }
        }

        oldest.stop()
        return oldest
    }

    private fun processCommands() {
        while (true) {
            when (val command = commands.poll() ?: break) {
                is Command.NoteOn -> {
                    if (command.velocity > 0f) {
                        allocateVoice().playNote(
                            sample = sample,
                            targetMidi = command.note,
                            velocity = command.velocity,
                            loop = loopRegion
                        )
                    }
                }

                is Command.NoteOff -> {
                    for (voice in voices) {
                        if (voice.isActive &&
                            voice.midiNote == command.note
                        ) {
                            voice.noteOff()
                        }
                    }
                }

                Command.AllNotesOff -> {
                    for (voice in voices) {
                        voice.noteOff()
                    }
                }
            }
        }
    }

    /**
     * output is interleaved stereo:
     * L, R, L, R, ...
     */
    fun render(output: FloatArray, frames: Int) {
        require(frames >= 0)
        require(output.size >= frames * 2)

        processCommands()

        val size = frames * 2

        for (i in 0 until size) {
            output[i] = 0f
        }

        for (frame in 0 until frames) {
            val offset = frame * 2

            for (voice in voices) {
                voice.render(output, offset)
            }

            output[offset] *= masterVolume
            output[offset + 1] *= masterVolume

            // Safety clipping.
            output[offset] =
                output[offset].coerceIn(-1f, 1f)

            output[offset + 1] =
                output[offset + 1].coerceIn(-1f, 1f)
        }
    }
}
