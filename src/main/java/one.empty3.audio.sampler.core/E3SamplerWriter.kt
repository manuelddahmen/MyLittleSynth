
package one.empty3.audio.sampler.io

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object E3SamplerWriter {

    private val json = Json {
        prettyPrint = true
    }

    data class ZoneFile(
        val manifest: ZoneManifest,
        val wavBytes: ByteArray
    )

    fun write(
        output: OutputStream,
        name: String,
        author: String,
        zones: List<ZoneFile>,
        polyphony: Int = 32,
        masterVolume: Float = 0.8f
    ) {
        require(zones.isNotEmpty())
        require(zones.size <= 1024)
        require(name.isNotBlank())
        require(polyphony in 1..256)
        require(masterVolume.isFinite() && masterVolume >= 0f)

        val manifest = InstrumentManifest(
            name = name,
            author = author,
            polyphony = polyphony,
            masterVolume = masterVolume,
            zones = zones.map { it.manifest }
        )

        val manifestText = json.encodeToString(manifest)

        ZipOutputStream(output).use { zip ->

            zip.putNextEntry(
                ZipEntry("instrument.json")
            )

            zip.write(manifestText.encodeToByteArray())
            zip.closeEntry()

            for (zone in zones) {
                val path = zone.manifest.sample

                require(
                    path.startsWith("samples/") &&
                    !path.contains("..") &&
                    !path.startsWith("/")
                )

                zip.putNextEntry(ZipEntry(path))
                zip.write(zone.wavBytes)
                zip.closeEntry()
            }
        }
    }
}
