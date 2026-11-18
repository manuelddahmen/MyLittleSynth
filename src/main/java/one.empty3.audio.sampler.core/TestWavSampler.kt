val input = java.io.FileInputStream("piano.wav")

val sample = WavReader.read(
    input = input,
    rootMidi = 60
)

input.close()

val sampler = Sampler(
    sample = sample,
    outputSampleRate = 44100,
    polyphony = 16
)

sampler.masterVolume = 0.8f

val output = DesktopAudioOutput(
    sampler = sampler,
    sampleRate = 44100
)

output.start()

// Jouer un Do central
sampler.noteOn(60, 1.0f)

// Jouer un Mi
sampler.noteOn(64, 0.8f)

// Jouer un Sol
sampler.noteOn(67, 0.8f)

// Relâcher les notes
sampler.noteOff(60)
sampler.noteOff(64)
sampler.noteOff(67)

// Arrêter la sortie
output.stop()