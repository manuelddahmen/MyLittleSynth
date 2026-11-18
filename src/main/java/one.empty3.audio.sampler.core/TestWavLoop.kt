sampler.loopRegion = LoopRegion(
    startFrame = 12000,
    endFrame = 48000,
    enabled = true
)

sampler.noteOn(60, 1.0f)

// La boucle continue pendant la note.
sampler.noteOff(60)

// La voix sort de la boucle et joue son Release.