package one.empty3.audio.sampler.core

import one.empty3.audio.sampler.core.SampleData
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavReader {

    @JvmStatic
    @JvmOverloads
    fun read(
        input: InputStream,
        rootMidi: Int = 60
    ): SampleData {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8192)

        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (count == 0) continue
            out.write(buffer, 0, count)
        }

        return decode(out.toByteArray(), rootMidi)
    }

    @JvmStatic
    @JvmOverloads
    fun decode(
        bytes: ByteArray,
        rootMidi: Int = 60
    ): SampleData {
        require(bytes.size >= 44)

        val bb = ByteBuffer.wrap(bytes)
            .order(ByteOrder.LITTLE_ENDIAN)

        fun fourCC(offset: Int) =
            String(bytes, offset, 4, Charsets.US_ASCII)

        require(fourCC(0) == "RIFF")
        require(fourCC(8) == "WAVE")

        var format = 0
        var channels = 0
        var sampleRate = 0
        var bits = 0
        var blockAlign = 0

        var dataOffset = -1
        var dataSize = 0

        var offset = 12

        while (offset + 8 <= bytes.size) {
            val id = fourCC(offset)
            val chunkSize =
                bb.getInt(offset + 4).toLong() and 0xffffffffL

            val content = offset + 8

            require(chunkSize <= Int.MAX_VALUE)

            val size = chunkSize.toInt()

            require(content.toLong() + size <= bytes.size)

            when (id) {
                "fmt " -> {
                    require(size >= 16)

                    format = bb.getShort(content)
                        .toInt() and 0xffff

                    channels = bb.getShort(content + 2)
                        .toInt() and 0xffff

                    sampleRate = bb.getInt(content + 4)

                    blockAlign = bb.getShort(content + 12)
                        .toInt() and 0xffff

                    bits = bb.getShort(content + 14)
                        .toInt() and 0xffff

                    // WAVE_FORMAT_EXTENSIBLE:
                    // Read the embedded subformat tag.
                    if (format == 0xfffe) {
                        require(size >= 40)
                        format = bb.getShort(content + 24)
                            .toInt() and 0xffff
                    }
                }

                "data" -> {
                    if (dataOffset < 0) {
                        dataOffset = content
                        dataSize = size
                    }
                }
            }

            offset = content + size + (size and 1)
        }

        require(format == 1 || format == 3) {
            "Unsupported WAV encoding: $format"
        }

        require(channels == 1 || channels == 2)
        require(sampleRate > 0)
        require(bits in if (format == 1)
            listOf(8, 16, 24, 32)
            else listOf(32)
        )
        require(blockAlign >= channels * (bits / 8))
        require(dataOffset >= 0)

        val bytesPerSample = bits / 8
        val frames = dataSize / blockAlign

        val samples = FloatArray(frames * channels)

        for (frame in 0 until frames) {
            for (channel in 0 until channels) {
                val pos = dataOffset +
                        frame * blockAlign +
                        channel * bytesPerSample

                val value = when {
                    format == 3 && bits == 32 -> {
                        bb.getFloat(pos)
                    }

                    bits == 8 -> {
                        ((bb.get(pos).toInt() and 0xff) - 128) /
                                128f
                    }

                    bits == 16 -> {
                        bb.getShort(pos).toFloat() / 32768f
                    }

                    bits == 24 -> {
                        var value =
                            (bb.get(pos).toInt() and 0xff) or
                            ((bb.get(pos + 1).toInt() and 0xff) shl 8) or
                            ((bb.get(pos + 2).toInt() and 0xff) shl 16)

                        if ((value and 0x800000) != 0) {
                            value = value or -0x1000000
                        }

                        value.toFloat() / 8388608f
                    }

                    else -> {
                        bb.getInt(pos).toFloat() /
                                2147483648.0f
                    }
                }

                samples[frame * channels + channel] =
                    value.coerceIn(-1f, 1f)
            }
        }

        return SampleData(
            samples = samples,
            sampleRate = sampleRate,
            channels = channels,
            rootMidi = rootMidi
        )
    }
}
