package one.empty3.audio.sampler.core

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class Sampler @JvmOverloads constructor(
    val instrument: SamplerInstrument,
    private val outputSampleRate: Int = 44100
) {
    private var useDynamicZones: Boolean = false

    @Volatile
    var rootSample: SampleData? = null
        private set

    @Volatile
    var chromaticMode: Boolean = true

    private val noteSamples = ConcurrentHashMap<Int, SampleData>()
    private val noteLoops = ConcurrentHashMap<Int, LoopRegion>()

    var attack: Float = 0.01f
    var decay: Float = 0.15f
    var sustain: Float = 0.75f
    var release: Float = 0.25f

    var loopRegion: LoopRegion? = null

    @JvmOverloads
    constructor(
        sample: SampleData? = null,
        outputSampleRate: Int = 44100,
        polyphony: Int = 16
    ) : this(
        instrument = createDefaultInstrument(sample, polyphony),
        outputSampleRate = outputSampleRate
    ) {
        this.useDynamicZones = true
        this.rootSample = sample
    }

    companion object {
        private fun createDefaultInstrument(sample: SampleData?, polyphony: Int): SamplerInstrument {
            val effectiveSample = sample ?: SampleData(FloatArray(4), 44100, 1, 60)
            return SamplerInstrument(
                name = "DefaultSamplerInstrument",
                zones = listOf(
                    SampleZone(
                        name = "DefaultZone",
                        sample = effectiveSample,
                        keyLow = 0,
                        keyHigh = 127,
                        rootMidi = effectiveSample.rootMidi
                    )
                ),
                polyphony = polyphony.coerceIn(1, 256)
            )
        }
    }

    private val voices = Array(instrument.polyphony) {
        SamplerVoice(outputSampleRate)
    }

    private sealed interface Command {
        data class NoteOn(
            val note: Int,
            val velocity: Int
        ) : Command

        data class NoteOff(
            val note: Int
        ) : Command

        data object AllNotesOff : Command
    }

    private val commands =
        ConcurrentLinkedQueue<Command>()

    @Volatile
    var masterVolume: Float = instrument.masterVolume

    @JvmOverloads
    fun setRootSample(sample: SampleData, loop: LoopRegion? = null) {
        this.useDynamicZones = true
        this.rootSample = sample
        this.loopRegion = loop
    }

    @JvmOverloads
    fun assignNoteSample(midiNote: Int, sample: SampleData, loop: LoopRegion? = null) {
        this.useDynamicZones = true
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
        useDynamicZones = true
        rootSample = null
        loopRegion = null
        noteSamples.clear()
        noteLoops.clear()
    }

    fun hasSampleForNote(midiNote: Int): Boolean {
        if (!useDynamicZones) {
            return instrument.findZone(midiNote, 100) != null
        }
        if (noteSamples.containsKey(midiNote)) return true
        return chromaticMode && rootSample != null
    }

    fun hasAnySample(): Boolean {
        if (!useDynamicZones) {
            return instrument.zones.isNotEmpty()
        }
        return rootSample != null || noteSamples.isNotEmpty()
    }

    fun getAssignedNotes(): Set<Int> = noteSamples.keys.toSet()

    fun getSampleForNote(midiNote: Int): SampleData? {
        if (!useDynamicZones) {
            return instrument.findZone(midiNote, 100)?.sample
        }
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
        require(note in 0..127)

        val midiVelocity =
            (velocity.coerceIn(0f, 1f) * 127f)
                .toInt()
                .coerceIn(0, 127)

        if (midiVelocity == 0) {
            noteOff(note)
        } else {
            commands.add(
                Command.NoteOn(note, midiVelocity)
            )
        }
    }

    fun noteOff(note: Int) {
        commands.add(Command.NoteOff(note))
    }

    fun allNotesOff() {
        commands.add(Command.AllNotesOff)
    }

    private fun allocateVoice(): SamplerVoice {
        voices.firstOrNull { !it.isActive }?.let {
            return it
        }

        val oldest = voices.minByOrNull { it.age }
            ?: error("No sampler voices available")

        oldest.stop()
        return oldest
    }

    private fun resolveZone(note: Int, midiVelocity: Int): SampleZone? {
        if (useDynamicZones) {
            val directSample = noteSamples[note]
            val targetSample = directSample ?: if (chromaticMode) rootSample else null
            if (targetSample == null) return null
            val rawLoop = if (directSample != null) noteLoops[note] else loopRegion
            val validLoop = rawLoop?.takeIf { it.enabled && it.isValid(targetSample) }
            return SampleZone(
                name = "DynamicZone-$note",
                sample = targetSample,
                keyLow = 0,
                keyHigh = 127,
                velocityLow = 1,
                velocityHigh = 127,
                rootMidi = targetSample.rootMidi,
                envelope = AdsrSettings(
                    attack = attack,
                    decay = decay,
                    sustain = sustain,
                    release = release
                ),
                loop = validLoop
            )
        }

        val found = instrument.findZone(note, midiVelocity) ?: return null
        val overrideLoop = loopRegion?.takeIf { it.enabled && it.isValid(found.sample) }
        return if (overrideLoop != null) found.copy(loop = overrideLoop) else found
    }

    private fun processCommands() {
        while (true) {
            when (val command = commands.poll() ?: break) {
                is Command.NoteOn -> {
                    val zone = resolveZone(
                        command.note,
                        command.velocity
                    )

                    if (zone != null) {
                        allocateVoice().playNote(
                            zone = zone,
                            targetMidi = command.note,
                            velocity = command.velocity / 127f
                        )
                    }
                }

                is Command.NoteOff -> {
                    voices
                        .filter {
                            it.isActive &&
                            it.midiNote == command.note
                        }
                        .forEach {
                            it.noteOff()
                        }
                }

                Command.AllNotesOff -> {
                    voices.forEach {
                        it.noteOff()
                    }
                }
            }
        }
    }

    /**
     * Interleaved stereo output.
     */
    fun render(
        output: FloatArray,
        frames: Int
    ) {
        require(frames >= 0)
        require(output.size >= frames * 2)

        processCommands()

        val master = masterVolume.coerceAtLeast(0f)

        val size = frames * 2
        for (i in 0 until size) {
            output[i] = 0f
        }

        for (voice in voices) {
            voice.render(
                output,
                0,
                frames = frames
            )
        }

        for (i in 0 until size) {
            output[i] = (output[i] * master).coerceIn(-1f, 1f)
        }
    }

    fun getActiveVoiceCount(): Int =
        voices.count { it.isActive }
}
