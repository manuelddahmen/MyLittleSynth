package be.manudahmen.mylittlesynth.rythms;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.BiConsumer;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class ThreadPlaying extends Thread {
    private Timeline timeline = null;
    private boolean running = true;
    private RythmPanel.LoopTimer timer;

    public ThreadPlaying(RythmPanel.LoopTimer loopTimer, Timeline timeline) {
        this.timer = loopTimer;
        this.timeline = timeline;
    }

    public synchronized void run() {
        while (running) {
            this.timeline.getTimes().forEach(
                    (timeOnTimelinePC, file) -> {

                        double timeOnTimelineSec = timeOnTimelinePC * timeline.getDuration();
                            if (timeOnTimelineSec < timer.getCurrentTimeOnLineSec() &&
                            timeOnTimelineSec>timeline.hasPlayed(file))
                            {
                                try {
                                    System.out.println("Play at " + timeOnTimelineSec + " on timeline of duration" + timeline.getDuration());
                                    timeline.setPlayed(file);
                                    (new PlayWave(AudioSystem.getAudioInputStream(file))).start();
                                } catch (UnsupportedAudioFileException | IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                    });
        }

    }
}
