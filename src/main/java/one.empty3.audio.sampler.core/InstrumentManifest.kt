
package one.empty3.audio.sampler.io

import kotlinx.serialization.Serializable

@Serializable
data class InstrumentManifest(
    val format: String = "e3sampler",
    val version: Int = 1,
    val name: String,
    val author: String = "",
    val rootMidi: Int = 60,
    val polyphony: Int = 32,
    val masterVolume: Float = 0.8f,
    val zones: List<ZoneManifest>
)

@Serializable
data class ZoneManifest(
    val name: String,
    val sample: String,
    val keyLow: Int = 0,
    val keyHigh: Int = 127,
    val rootMidi: Int = 60,
    val velocityLow: Int = 1,
    val velocityHigh: Int = 127,
    val attack: Float = 0.01f,
    val decay: Float = 0.15f,
    val sustain: Float = 0.8f,
    val release: Float = 0.3f,
    val volume: Float = 1f,
    val loop: LoopManifest? = null
)

@Serializable
data class LoopManifest(
    val enabled: Boolean = false,
    val startFrame: Int = 0,
    val endFrame: Int = 0
)
