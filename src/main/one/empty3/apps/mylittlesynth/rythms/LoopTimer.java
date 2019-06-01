package one.empty3.apps.mylittlesynth.rythms;

import one.empty3.apps.mylittlesynth.Timer;

public class LoopTimer extends Timer {
    private final RythmPanel panel;

    public LoopTimer(RythmPanel rythmPanel)
    {
        super();
        this.panel = rythmPanel;
    }
    // TODO Je crois que le problème vient de cette classe avec des long de grandes tailles, le calcul en double et le reste en double en 0.0 et la durée de la loop
    public double getCurrentTimeOnLineSec() {
        return Math.IEEEremainder(getTotalTimeElapsedSec(), panel.timelineTimeSec())+panel.timelineTimeSec()/2;
    }
}
