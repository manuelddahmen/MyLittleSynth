package be.manudahmen.mylittlesynth.rythms;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TimelineThread extends Thread {
    public static final int ENDS = 0;
    private Timeline timeline;
    private double TINCR = 0.001;
    public TimelineThread(Timeline tImeline)
    {
        this.timeline = tImeline;

    }
    public void run()
    {
        double t = 0;
        int millis = 20;
        while(isRunning())
        {
            t = timeline.getRythmPanel().loopTimer.getCurrentTimeOnLineSec();
            while(t<timeline.getDuration())
            {

                t = timeline.getRythmPanel().loopTimer.getCurrentTimeOnLineSec();
                Timeline.Model next = timeline.getNext();
                if(next != null) {
                    if (next.timeOnTimeline > t) {
                        try {
                            PlayWave playWave = new PlayWave(next, AudioSystem.getAudioInputStream(
                                    next.wave), this);
                            playWave.start();
                            timeline.setTextTimeOnTimeline(next.wave);
                            while(playWave.isRunning())
                            {
                                Thread.sleep(millis);
                            }
                            timeline.hasPlayed(millis);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (UnsupportedAudioFileException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    private boolean isRunning() {
        return true;
    }

    public void del(Timeline.Model model) {
        timeline.remove(model);
        timeline.add(model);
    }

    public void start(Timeline.Model model) {
    }
}
