package one.empty3.audio.sampler.core

import one.empty3.audio.sampler.core.AdsrEnvelope
import one.empty3.audio.sampler.core.SampleData
import one.empty3.audio.sampler.core.SampleZone
import kotlin.math.floor
import kotlin.math.pow

/**
 * Une voix du sampler.
 *
 * Une instance correspond à une note MIDI en cours de lecture.
 *
 * La sortie audio est toujours stéréo et entrelacée :
 * L0, R0, L1, R1, ...
 */
class SamplerVoice(
    private val outputSampleRate: Int
) {

    init {
        require(outputSampleRate > 0) {
            "outputSampleRate must be positive"
        }
    }

    private var sample: SampleData? = null
    private var zone: SampleZone? = null

    /** Position de lecture exprimée en frames source. */
    private var currentIndex: Double = 0.0

    /** Incrément de lecture pour une frame de sortie. */
    private var pitchIncrement: Double = 1.0

    /** Note MIDI actuellement jouée. -1 signifie aucune note. */
    var midiNote: Int = -1
        private set

    /** Vélocité MIDI normalisée entre 0 et 1. */
    var velocity: Float = 0f
        private set

    /** Volume propre à la zone. */
    private var volume: Float = 1f

    /** Indique si la boucle est actuellement active. */
    private var looping: Boolean = false

    private var loopStart: Int = 0
    private var loopEnd: Int = 0

    /**
     * Compteur local de déclenchements.
     *
     * Pour comparer l'ancienneté de plusieurs voix, le Sampler
     * devra idéalement fournir un compteur global.
     */
    var age: Long = 0L
        private set

    private var ageCounter: Long = 0L

    private val envelope = AdsrEnvelope(outputSampleRate)

    val isActive: Boolean
        get() = sample != null &&
                midiNote >= 0 &&
                envelope.isActive

    /**
     * Déclenche une note.
     *
     * @param zone zone de l'instrument correspondant à la note
     * @param targetMidi note MIDI demandée
     * @param velocity vélocité MIDI comprise entre 0 et 1
     */
    fun playNote(
        zone: SampleZone,
        targetMidi: Int,
        velocity: Float
    ) {
        require(targetMidi in 0..127) {
            "Invalid MIDI note: $targetMidi"
        }

        val source = zone.sample

        require(source.frameCount > 0) {
            "Cannot play an empty sample"
        }

        // Réinitialisation de la voix précédente.
        stop()

        this.zone = zone
        this.sample = source

        this.midiNote = targetMidi
        this.velocity = velocity.coerceIn(0f, 1f)
        this.volume = zone.volume.coerceAtLeast(0f)

        currentIndex = 0.0

        /*
         * Rapport de fréquence :
         *
         * 2 ^ ((note demandée - note fondamentale) / 12)
         *
         * Multiplié par le rapport des fréquences
         * d'échantillonnage source / sortie.
         */
        val semitoneRatio = 2.0.pow(
            (targetMidi - zone.rootMidi) / 12.0
        )

        pitchIncrement =
            semitoneRatio *
            source.sampleRate.toDouble() /
            outputSampleRate.toDouble()

        // Configuration de la boucle.
        val loop = zone.loop

        looping = loop != null &&
                loop.enabled &&
                loop.startFrame >= 0 &&
                loop.endFrame <= source.frameCount &&
                loop.endFrame > loop.startFrame

        if (looping) {
            loopStart = loop!!.startFrame
            loopEnd = loop.endFrame
        } else {
            loopStart = 0
            loopEnd = 0
        }

        // Configuration de l'enveloppe ADSR.
        envelope.setSampleRate(outputSampleRate)

        envelope.attack = zone.envelope.attack
        envelope.decay = zone.envelope.decay
        envelope.sustain = zone.envelope.sustain
        envelope.release = zone.envelope.release

        envelope.noteOn()

        ageCounter++
        age = ageCounter
    }

    /**
     * Relâche la note MIDI.
     *
     * Important :
     * On ne supprime pas immédiatement le sample.
     * La voix continue de jouer pendant le Release de l'ADSR.
     */
    fun noteOff() {
        if (sample == null || midiNote < 0) {
            return
        }

        // Après Note Off, on ne répète plus la boucle.
        looping = false

        envelope.noteOff()
    }

    /**
     * Arrêt immédiat de la voix.
     *
     * Contrairement à noteOff(), cette méthode ne joue
     * pas la phase Release.
     */
    fun stop() {
        envelope.reset()

        sample = null
        zone = null

        midiNote = -1
        velocity = 0f
        volume = 1f

        currentIndex = 0.0
        pitchIncrement = 1.0

        looping = false
        loopStart = 0
        loopEnd = 0
    }

    /**
     * Rend les frames audio dans un buffer stéréo entrelacé.
     *
     * @param output buffer de sortie FloatArray
     * @param outputOffset indice de départ dans le buffer,
     *                     exprimé en nombre de valeurs Float
     * @param frames nombre de frames stéréo à produire
     *
     * Le buffer doit contenir au minimum :
     * outputOffset + frames * 2 valeurs.
     */
    @JvmOverloads
    fun render(
        output: FloatArray,
        outputOffset: Int,
        frames: Int = 1
    ) {
        val source = sample ?: return

        if (frames <= 0) return

        require(outputOffset >= 0)
        require(outputOffset + frames * 2 <= output.size) {
            "Output buffer is too small"
        }

        for (i in 0 until frames) {

            /*
             * 1. Vérifier si l'enveloppe est terminée.
             *
             * Ce test est effectué au début de chaque frame.
             * Ainsi, la dernière frame du Release a déjà été
             * écrite dans le buffer avant l'arrêt de la voix.
             */
            if (!envelope.isActive) {
                stop()
                return
            }

            /*
             * 2. Vérifier la position de lecture.
             */
            if (looping) {
                normalizeLoopPosition()
            } else if (currentIndex >= source.frameCount) {
                stop()
                return
            }

            /*
             * 3. Calculer le niveau de l'enveloppe ADSR.
             */
            val envelopeLevel = envelope.nextLevel()

            /*
             * 4. Lire le sample avec interpolation linéaire.
             */
            val left = readInterpolated(
                source,
                currentIndex,
                0
            )

            val right = readInterpolated(
                source,
                currentIndex,
                if (source.channels > 1) 1 else 0
            )

            /*
             * 5. Appliquer vélocité, volume et enveloppe.
             */
            val gain = envelopeLevel *
                    velocity *
                    volume

            /*
             * 6. Mélanger dans la sortie stéréo.
             */
            val outputIndex = outputOffset + i * 2

            output[outputIndex] += left * gain
            output[outputIndex + 1] += right * gain

            /*
             * 7. Avancer dans le sample.
             */
            currentIndex += pitchIncrement
        }
    }

    /**
     * Interpolation linéaire d'un canal du sample.
     */
    private fun readInterpolated(
        source: SampleData,
        position: Double,
        channel: Int
    ): Float {

        val index0 = floor(position).toInt()

        val fraction = (position - index0).toFloat()

        val index1 = when {
            looping &&
                    index0 >= loopStart &&
                    index0 + 1 >= loopEnd -> loopStart

            index0 + 1 < source.frameCount -> index0 + 1

            else -> index0
        }

        val sample0 = source.getSample(
            index0.coerceIn(0, source.frameCount - 1),
            channel
        )

        val sample1 = source.getSample(
            index1.coerceIn(0, source.frameCount - 1),
            channel
        )

        return sample0 + (sample1 - sample0) * fraction
    }

    /**
     * Ramène la position de lecture dans la région de boucle.
     */
    private fun normalizeLoopPosition() {
        if (!looping) return

        if (currentIndex >= loopEnd) {
            val loopLength = loopEnd - loopStart

            if (loopLength > 0) {
                val overflow = currentIndex - loopStart

                currentIndex =
                    loopStart + (overflow % loopLength)
            }
        }
    }
}