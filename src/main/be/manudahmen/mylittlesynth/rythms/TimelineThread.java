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

    public void run()
    {
        while(isRunning())
        {
            double t = 0;
            while(t<timeline.getDuration())
            {
                Timeline.Model next = timeline.getNext();
                if(next.timeOnTimeline>t) {
                    try {
                        (new PlayWave(next, AudioSystem.getAudioInputStream(
                                next.wave), this)
                        ).start();
                        Thread.sleep(10);
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

    private boolean isRunning() {
        return true;
    }

    public void del(Timeline.Model model) {
        timeline.queue(model);
    }

    public void start(Timeline.Model model) {
        timeline.remove(model);
    }
}
