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
    private LoopTimer timer;

    public ThreadPlaying(LoopTimer loopTimer, Timeline timeline) {
        this.timer = loopTimer;
        this.timeline = timeline;
        timeline.init();
    }

    public synchronized void run() {

        while (running) {

            Timeline.Model timeOnTimelinePC = timeline.getNext();
            if (timeOnTimelinePC != null) {
                double timeOnTimelineSec = timeOnTimelinePC.timeOnTimeline * timeline.getDuration();

                while (timer.getCurrentTimeOnLineSec() < timeOnTimelineSec) {
                    //try {
                    System.out.printf("Play %s at %f on timeline of duration %f\n",
                            timeOnTimelinePC.wave.getName(), timeOnTimelineSec, timeline.getDuration());
                      /* (new PlayWave(AudioSystem.getAudioInputStream(
                                timeOnTimelinePC.wave
                        ))).start();
                    } catch (UnsupportedAudioFileException | IOException ex) {
                        ex.printStackTrace();
                    }*/
                }
            }
        }
    }


}