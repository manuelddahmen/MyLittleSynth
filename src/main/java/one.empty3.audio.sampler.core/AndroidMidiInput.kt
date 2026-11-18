package one.empty3.audio.sampler.core

import android.media.midi.MidiReceiver

class AndroidMidiInput : MidiInput {

    private var listener: MidiMessageListener? = null

    private val receiver = object : MidiReceiver() {
        override fun onSend(
            msg: ByteArray,
            offset: Int,
            count: Int,
            timestamp: Long
        ) {
            val end = offset + count
            var runningStatus = -1
            var i = offset

            while (i < end) {
                val value = msg[i].toInt() and 0xff

                if (value >= 0x80) {
                    runningStatus = value
                    i++
                } else if (runningStatus >= 0x80) {
                    val command = runningStatus and 0xF0

                    if (command == 0xC0 || command == 0xD0) {
                        // One data byte messages.
                        listener?.onMessage(
                            runningStatus, value, 0
                        )
                        i++
                    } else if (i + 1 < end) {
                        val data2 =
                            msg[i + 1].toInt() and 0xff

                        listener?.onMessage(
                            runningStatus,
                            value,
                            data2
                        )

                        i += 2
                    } else {
                        break
                    }
                } else {
                    i++
                }
            }
        }
    }

    override fun start(listener: MidiMessageListener) {
        this.listener = listener
    }

    override fun stop() {
        listener = null
    }

    fun getReceiver(): MidiReceiver = receiver
}
