package be.manudahmen.mylittlesynth.rythms;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
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


                    Media hit = new Media(next.wave.toURI().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(hit);
                    mediaPlayer.play();
                    timeline.hasPlayed(millis);
                    timeline.queue(next);

                }
            }

        }
    }

    private boolean isRunning() {
        return true;
    }

}
