package be.manudahmen.mylittlesynth;

public class Note {
    private int tone;
    private SoundProductionSystem.Waveform waveform;
    private Enveloppe enveloppe;
    private Timer timer;
    private long minDuration;
    private boolean finish;
    private long position;
    private long definitivePosition;

    public Note(long minDuration, int tone, SoundProductionSystem.Waveform waveform, Enveloppe enveloppe) {
        this.minDuration = minDuration;
        this.tone = tone;
        this.waveform = waveform;
        this.enveloppe = enveloppe;
        position = 0;
        init();

    }

    public double getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(long minDuration) {
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

    public void init() {
        this.timer = new Timer();
        timer.init();
        enveloppe.setTimer(timer);
    }
    public Timer getTimer() {
        return timer;
    }

    public boolean isFinish() {
        return timer.getDefinitiveTime() > timer.getTotalTimeElapsed();
    }

    public long getPosition() {
        position = System.nanoTime() - getTimer().getInitTime();
        return position;
    }

    public void play() {
        position = 0;
        timer.init();
    }

    public void stop() {

        timer.stop();
        definitivePosition = position;
    }

    public void positionInc() {
        position++;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public long getDefinitivePosition() {
        return definitivePosition;
    }

    public void setDefinitivePosition(long definitivePosition) {
        this.definitivePosition = definitivePosition;
    }
}
