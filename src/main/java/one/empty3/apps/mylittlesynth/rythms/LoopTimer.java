package one.empty3.apps.mylittlesynth.rythms;

import one.empty3.apps.mylittlesynth.Timer;

public class LoopTimer extends Timer {
    private final RythmPanel panel;
    private final int track;
    private double lastTimeSec;
    private int loop;

    public LoopTimer(RythmPanel rythmPanel, int track)
    {
        super();
        this.panel = rythmPanel;
        this.track = track;
    }
    public double getCurrentTimeOnLineSec() {
        double ieeEremainder = Math.IEEEremainder(getTotalTimeElapsedNanoSec() / 1000000000.0, panel.timelineTimeSec(track))+panel.timelineTimeSec(track)/2.0;
        if(ieeEremainder<lastTimeSec)
            loop++;
        this.lastTimeSec = ieeEremainder;
        return ieeEremainder;
    }
    public double getLastTimeSec()
    {
        return lastTimeSec;
    }

    public void setLastTimeSec(double lastTimeSec) {
        this.lastTimeSec = lastTimeSec;
    }

    public RythmPanel getPanel() {
        return panel;
    }

    public int getLoop() {
        return loop;
    }

    public void setLoop(int loop) {
        this.loop = loop;
    }
}
