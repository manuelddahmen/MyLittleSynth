package be.manudahmen.mylittlesynth.rythms;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class TimelineThread extends Thread {
    public static final int ENDS = 0;
    public Timeline timeline;
    private double TINCR = 0.001;

    public TimelineThread(Timeline tImeline) {
        this.timeline = tImeline;

    }

    public void run() {
        double t = 0;
        int millis = 100;
        while (isRunning()) {
            t = timeline.getRythmPanel().loopTimer.getCurrentTimeOnLineSec();
            Timeline.Model next = timeline.next();
            if (next != null) {
                if (next.timeOnTimelinePC*timeline.getDuration() > t - 0.3 && next.timeOnTimelinePC*timeline.getDuration() < t + 0.3) {
                    try {

                        timeline.queue(next);

                        timeline.setTextTimeOnTimeline(next.wave);
                        PlayWave playWave = new PlayWave(next, AudioSystem.getAudioInputStream(
                                next.wave), this);
                        playWave.start();

                        timeline.hasPlayed(millis);
                    } catch (UnsupportedAudioFileException | IOException  e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private boolean isRunning() {
        return true;
    }

}
