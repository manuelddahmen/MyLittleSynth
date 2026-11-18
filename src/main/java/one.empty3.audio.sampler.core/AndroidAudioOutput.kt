package one.empty3.audio.sampler.core

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import one.empty3.audio.sampler.core.Sampler

class AndroidAudioOutput(
    private val sampler: Sampler,
    private val sampleRate: Int = 44100,
    private val blockFrames: Int = 256
) : AudioOutput {

    private var running = false
    private var worker: Thread? = null
    private var track: AudioTrack? = null

    override val isRunning: Boolean
        get() = running

    override fun start() {
        if (running) return

        val minBuffer = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_FLOAT
        )

        require(minBuffer > 0) {
            "Unsupported Android audio configuration"
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_MUSIC
                    )
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(
                        AudioFormat.CHANNEL_OUT_STEREO
                    )
                    .build()
            )
            .setBufferSizeInBytes(
                maxOf(minBuffer, blockFrames * 2 * 4 * 4)
            )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack.play()

        track = audioTrack
        running = true

        worker = Thread {
            val audio = FloatArray(blockFrames * 2)

            while (running) {
                sampler.render(audio, blockFrames)

                val written = audioTrack.write(
                    audio,
                    0,
                    audio.size,
                    AudioTrack.WRITE_BLOCKING
                )

                if (written < 0) {
                    running = false
                }
            }
        }.apply {
            name = "Empty3-Android-Audio"
            isDaemon = true
            start()
        }
    }

    override fun stop() {
        running = false

        worker?.join(1000)
        worker = null

        track?.pause()
        track?.flush()
        track?.stop()
        track?.release()
        track = null
    }
}
