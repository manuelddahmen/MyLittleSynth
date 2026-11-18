package one.empty3.audio.sampler.core

import javax.sound.midi.MidiSystem
import javax.sound.midi.Receiver
import javax.sound.midi.ShortMessage

class DesktopMidiInput : MidiInput {

    private var receiver: Receiver? = null

    override fun start(listener: MidiMessageListener) {
        val device = MidiSystem.getMidiDeviceInfo()
            .asSequence()
            .map { MidiSystem.getMidiDevice(it) }
            .firstOrNull {
                it.maxTransmitters != 0
            } ?: error("No MIDI input device found")

        device.open()

        receiver = object : Receiver {
            override fun send(
                message: javax.sound.midi.MidiMessage,
                timeStamp: Long
            ) {
                if (message is ShortMessage) {
                    listener.onMessage(
                        message.status,
                        message.data1,
                        message.data2
                    )
                }
            }

            override fun close() {}
        }

        device.transmitter.receiver = receiver
    }

    override fun stop() {
        receiver?.close()
        receiver = null
    }
}
