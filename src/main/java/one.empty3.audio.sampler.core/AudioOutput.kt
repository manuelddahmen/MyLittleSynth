package one.empty3.audio.sampler.core

interface AudioOutput {
    fun start()

    fun stop()

    val isRunning: Boolean
}
