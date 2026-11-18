package one.empty3.audio.sampler.core

fun interface MidiMessageListener {
    fun onMessage(
        status: Int,
        data1: Int,
        data2: Int
    )
}

interface MidiInput {
    fun start(listener: MidiMessageListener)

    fun stop()
}
