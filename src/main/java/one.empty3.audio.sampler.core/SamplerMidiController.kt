
package one.empty3.audio.sampler.core

class SamplerMidiController(
    private val sampler: Sampler
) {
    fun onMidiMessage(
        status: Int,
        data1: Int,
        data2: Int
    ) {
        val command = status and 0xF0

        when (command) {
            0x90 -> {
                // Note On
                // Velocity zero means Note Off.
                if (data2 == 0) {
                    sampler.noteOff(data1)
                } else {
                    sampler.noteOn(
                        data1,
                        data2 / 127f
                    )
                }
            }

            0x80 -> {
                sampler.noteOff(data1)
            }

            0xB0 -> {
                // Control Change
                when (data1) {
                    7 -> {
                        sampler.masterVolume =
                            data2 / 127f
                    }

                    123 -> {
                        sampler.allNotesOff()
                    }
                }
            }
        }
    }
}
