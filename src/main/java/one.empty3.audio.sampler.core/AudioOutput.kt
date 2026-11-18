
package one.empty3.audio.sampler.output

interface AudioOutput {
    fun start()

    fun stop()

    val isRunning: Boolean
}
