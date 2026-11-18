package one.empty3.audio.sampler.core

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class Sampler @JvmOverloads constructor(
    sample: SampleData? = null,
    private val outputSampleRate: Int = 44100,
    polyphony: Int = 16
) {
    private val voices = Array(polyphony) {
        SamplerVoice(outputSampleRate)
    }

    @Volatile
    var rootSample: SampleData? = sample
        private set

    @Volatile
    var chromaticMode: Boolean = true

    private val noteSamples = ConcurrentHashMap<Int, SampleData>()
    private val noteLoops = ConcurrentHashMap<Int, LoopRegion>()

    var attack: Float = 0.01f
    var decay: Float = 0.15f
    var sustain: Float = 0.75f
    var release: Float = 0.25f

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

    @JvmOverloads
    fun setRootSample(sample: SampleData, loop: LoopRegion? = null) {
        this.rootSample = sample
        this.loopRegion = loop
    }

    @JvmOverloads
    fun assignNoteSample(midiNote: Int, sample: SampleData, loop: LoopRegion? = null) {
        noteSamples[midiNote] = sample
        if (loop != null) {
            noteLoops[midiNote] = loop
        } else {
            noteLoops.remove(midiNote)
        }
    }

    fun removeNoteSample(midiNote: Int) {
        noteSamples.remove(midiNote)
        noteLoops.remove(midiNote)
    }

    fun clearAllSamples() {
        allNotesOff()
        rootSample = null
        loopRegion = null
        noteSamples.clear()
        noteLoops.clear()
    }

    fun hasSampleForNote(midiNote: Int): Boolean {
        if (noteSamples.containsKey(midiNote)) return true
        return chromaticMode && rootSample != null
    }

    fun hasAnySample(): Boolean {
        return rootSample != null || noteSamples.isNotEmpty()
    }

    fun getAssignedNotes(): Set<Int> = noteSamples.keys.toSet()

    fun getSampleForNote(midiNote: Int): SampleData? {
        return noteSamples[midiNote] ?: if (chromaticMode) rootSample else null
    }

    fun setAdsr(attack: Float, decay: Float, sustain: Float, release: Float) {
        this.attack = attack.coerceAtLeast(0.001f)
        this.decay = decay.coerceAtLeast(0.001f)
        this.sustain = sustain.coerceIn(0f, 1f)
        this.release = release.coerceAtLeast(0.001f)
    }

    @JvmOverloads
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
                        val directSample = noteSamples[command.note]
                        val targetSample = directSample ?: if (chromaticMode) rootSample else null
                        if (targetSample != null) {
                            val activeLoop = if (directSample != null) {
                                noteLoops[command.note]
                            } else {
                                loopRegion
                            }
                            val voice = allocateVoice()
                            voice.envelope.attack = attack
                            voice.envelope.decay = decay
                            voice.envelope.sustain = sustain
                            voice.envelope.release = release
                            voice.playNote(
                                sample = targetSample,
                                targetMidi = command.note,
                                velocity = command.velocity,
                                loop = activeLoop
                            )
                        }
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
