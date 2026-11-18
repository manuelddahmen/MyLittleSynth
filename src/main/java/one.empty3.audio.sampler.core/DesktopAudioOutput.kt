package one.empty3.audio.sampler.core

import one.empty3.audio.sampler.core.Sampler
import javax.sound.sampled.*

class DesktopAudioOutput @JvmOverloads constructor(
    private val sampler: Sampler,
    private val sampleRate: Int = 44100,
    private val blockFrames: Int = 256
) : AudioOutput {

    private var running = false
    private var worker: Thread? = null
    private var line: SourceDataLine? = null

    override val isRunning: Boolean
        get() = running

    override fun start() {
        if (running) return

        val format = AudioFormat(
            sampleRate.toFloat(),
            16,
            2,
            true,
            false
        )

        val info = DataLine.Info(
            SourceDataLine::class.java,
            format
        )

        val audioLine =
            AudioSystem.getLine(info) as SourceDataLine

        audioLine.open(format, blockFrames * 4 * 4)
        audioLine.start()

        line = audioLine
        running = true

        worker = Thread {
            val audio = FloatArray(blockFrames * 2)
            val pcm = ByteArray(blockFrames * 4)

            while (running) {
                sampler.render(audio, blockFrames)

                for (i in audio.indices) {
                    val value = (audio[i] * 32767f)
                        .toInt()
                        .coerceIn(-32768, 32767)

                    pcm[i * 2] = value.toByte()
                    pcm[i * 2 + 1] =
                        (value shr 8).toByte()
                }

                audioLine.write(pcm, 0, pcm.size)
            }
        }.apply {
            name = "Empty3-Sampler-Audio"
            isDaemon = true
            start()
        }
    }

    override fun stop() {
        running = false

        worker?.join(1000)
        worker = null

        line?.stop()
        line?.close()
        line = null
    }
}
