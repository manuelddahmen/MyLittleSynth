package be.manudahmen.mylittlesynth;

public class Note {
    private int tone;
    private SoundProductionSystem.Waveform waveform;
    private Enveloppe enveloppe;
    private Timer timer;
    private double minDuration;
    private boolean finish;
    private long positionNIncr;

    public Note(double minDuration, int tone, SoundProductionSystem.Waveform waveform, Enveloppe enveloppe) {
        this.minDuration = minDuration;
        this.tone = tone;
        this.waveform = waveform;
        this.enveloppe = enveloppe;
        positionNIncr = 0;
        this.timer = new Timer();
        timer.init();
    }

    public double getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(double minDuration) {
        this.minDuration = minDuration;
    }

    public int getTone() {
        return tone;
    }

    public void setTone(int tone) {
        this.tone = tone;
    }

    public SoundProductionSystem.Waveform getWaveform() {
        return waveform;
    }

    public void setWaveform(SoundProductionSystem.Waveform waveform) {
        this.waveform = waveform;
    }

    public Enveloppe getEnveloppe() {
        return enveloppe;
    }

    public void setEnveloppe(Enveloppe enveloppe) {
        this.enveloppe = enveloppe;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
        timer.init();
    }

    public Timer getTimer() {
        return timer;
    }

    public boolean isFinish() {
        return getTimer().getTimeElapsed() >= this.getMinDuration();
    }

    public long getPositionNIncr() {
        return positionNIncr++;
    }
}
