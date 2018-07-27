package be.manudahmen.mylittlesynth;

public class Note {
    private int tone;
    private SoundProductionSystem.Waveform waveform;
    private Enveloppe enveloppe;
    private Timer timer;
    private double minDuration;
    private boolean finish;
    private long position;

    public Note(double minDuration, int tone, SoundProductionSystem.Waveform waveform, Enveloppe enveloppe) {
        this.minDuration = minDuration;
        this.tone = tone;
        this.waveform = waveform;
        this.enveloppe = enveloppe;
        position = 0;
        this.timer = new Timer();
        timer.init();
        enveloppe.setTimer(timer);
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
        return timer.getDefinitiveTime() > 0 || timer.getTotalTimeElapsed() >= getMinDuration();//getTimer().getTotalTimeElapsed() >= this.getMinDuration();
    }

    public long getPosition() {
        return position;
    }

    public void play() {
        Timer timer = new Timer();
        setTimer(timer);
        position = 0;
        timer.init();
    }

    public void stop() {
        timer.stop();
    }

    public void positionInc() {
        position++;
    }
}
