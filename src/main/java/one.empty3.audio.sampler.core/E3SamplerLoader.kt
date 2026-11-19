
package one.empty3.audio.sampler.io

import kotlinx.serialization.json.Json
import one.empty3.audio.sampler.core.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

object E3SamplerLoader {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = false
    }

    fun load(input: InputStream): SamplerInstrument {
        val files = HashMap<String, ByteArray>()

        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break

                if (!entry.isDirectory) {
                    val name = entry.name

                    validatePath(name)

                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)

                    var total = 0L

                    while (true) {
                        val count = zip.read(buffer)
                        if (count < 0) break
                        if (count == 0) continue

                        total += count

                        require(total <= MAX_ENTRY_SIZE) {
                            "Entry too large: $name"
                        }

                        output.write(buffer, 0, count)
                    }

                    require(files.put(name, output.toByteArray()) == null) {
                        "Duplicate archive entry: $name"
                    }
                }

                zip.closeEntry()
            }
        }

        val manifestBytes =
            files["instrument.json"]
                ?: error("Missing instrument.json")

        val manifest = json.decodeFromString(
            InstrumentManifest.serializer(),
            manifestBytes.decodeToString()
        )

        require(manifest.format == "e3sampler")
        require(manifest.version == 1)
        require(manifest.zones.isNotEmpty())
        require(manifest.zones.size <= MAX_ZONES)
        require(manifest.polyphony in 1..256)
        require(manifest.masterVolume.isFinite())
        require(manifest.masterVolume >= 0f)

        val zones = manifest.zones.map { zone ->
            validateZone(zone)

            val wavBytes = files[zone.sample]
                ?: error("Missing sample: ${zone.sample}")

            val sample = WavReader.decode(
                wavBytes,
                rootMidi = zone.rootMidi
            )

            val loop = zone.loop?.takeIf { it.enabled }?.let {
                LoopRegion(
                    startFrame = it.startFrame,
                    endFrame = it.endFrame
                )
            }

            SampleZone(
                name = zone.name,
                sample = sample,
                keyLow = zone.keyLow,
                keyHigh = zone.keyHigh,
                velocityLow = zone.velocityLow,
                velocityHigh = zone.velocityHigh,
                rootMidi = zone.rootMidi,
                envelope = AdsrSettings(
                    attack = zone.attack,
                    decay = zone.decay,
                    sustain = zone.sustain,
                    release = zone.release
                ),
                loop = loop,
                volume = zone.volume
            )
        }

        return SamplerInstrument(
            name = manifest.name,
            author = manifest.author,
            zones = zones,
            polyphony = manifest.polyphony,
            masterVolume = manifest.masterVolume
        )
    }

    private fun validateZone(zone: ZoneManifest) {
        require(zone.name.isNotBlank())
        validatePath(zone.sample)

        require(zone.keyLow in 0..127)
        require(zone.keyHigh in zone.keyLow..127)

        require(zone.velocityLow in 1..127)
        require(zone.velocityHigh in zone.velocityLow..127)

        require(zone.rootMidi in 0..127)

        require(zone.attack.isFinite() && zone.attack >= 0f)
        require(zone.decay.isFinite() && zone.decay >= 0f)
        require(zone.sustain.isFinite() && zone.sustain in 0f..1f)
        require(zone.release.isFinite() && zone.release >= 0f)

        require(zone.volume.isFinite() && zone.volume >= 0f)

        zone.loop?.let {
            require(it.startFrame >= 0)
            require(it.endFrame >= 0)
        }
    }

    private fun validatePath(path: String) {
        require(path.isNotBlank())
        require('\\' !in path)
        require(!path.startsWith("/"))
        require(!path.contains(':'))

        val segments = path.split('/')

        require(segments.none {
            it == ".." || it == "." || it.isEmpty()
        }) {
            "Invalid archive path: $path"
        }
    }

    private const val MAX_ENTRY_SIZE = 128L * 1024 * 1024
    private const val MAX_ZONES = 1024
}
