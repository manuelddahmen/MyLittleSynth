package be.manudahmen.mylittlesynth.rythms;

import be.manudahmen.mylittlesynth.Timer;

public class LoopTimer extends Timer {
    private final RythmPanel panel;

    public LoopTimer(RythmPanel rythmPanel)
    {
        super();
        this.panel = rythmPanel;
    }

    public double getCurrentTimeOnLineSec() {
        double remainder = Math.IEEEremainder((getTotalTimeElapsedNanoSec()),
                panel.timelineTimeSec()*1E9d)/1E9d;
        return remainder;
    }
}
