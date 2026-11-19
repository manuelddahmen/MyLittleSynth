package one.empty3.apps.mylittlesynth

import one.empty3.audio.sampler.core.Sampler
import one.empty3.audio.sampler.io.E3SamplerLoader
import one.empty3.audio.sampler.io.E3SamplerWriter
import java.io.File
import java.io.FileInputStream

fun demoLoadSaveInstrument(
    inputStream: java.io.InputStream,
    file: File,
    zoneFiles: List<E3SamplerWriter.ZoneFile>
) {
    val instrument = E3SamplerLoader.load(
        FileInputStream("GrandPiano.e3sampler")
    )

    val sampler = Sampler(
        instrument = instrument,
        outputSampleRate = 48000
    )

    sampler.noteOn(60, 0.5f)
    sampler.noteOn(64, 0.8f)
    sampler.noteOn(67, 1.0f)

    sampler.noteOff(60)

    val loadedFromStream = E3SamplerLoader.load(inputStream)

    // Création d'une nouvelle archive
    E3SamplerWriter.write(
        output = file.outputStream(),
        name = loadedFromStream.name.ifBlank { "Grand Piano" },
        author = "Empty3",
        zones = zoneFiles
    )
}
