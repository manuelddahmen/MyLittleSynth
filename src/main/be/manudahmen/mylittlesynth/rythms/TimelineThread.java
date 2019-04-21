package be.manudahmen.mylittlesynth.rythms;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TimelineThread extends Thread {
    public static final int ENDS = 0;
    public Timeline[] timeline;
    private double TINCR = 0.001;
    private boolean pause = false;

    public int getLoop() {
        return loop;
    }

    public void setLoop(int loop) {
        this.loop = loop;
    }

    private int loop;

    public TimelineThread(Timeline[] tImeline) {
        this.timeline = tImeline;

    }

    public void run() {
        pause = false;
        double t = 0;
        int millis = 100;
        while (isRunning()) {
            while(pause)
            {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                t = timeline[loop].
                        getRythmPanel().
                        loopTimer[
                        timeline[loop].
                                getRythmPanel().
                                loop].
                        getCurrentTimeOnLineSec();
                Timeline.Model next = timeline[loop].next();

                if (next != null) {
                    if (next.timeOnTimelinePC * timeline[loop].getDuration() > t - 0.3 && next.timeOnTimelinePC * timeline[loop].getDuration() < t + 0.3) {


                        Media hit = new Media(next.wave.toURI().toString());
                        MediaPlayer mediaPlayer = new MediaPlayer(hit);
                        mediaPlayer.play();
                        timeline[loop].hasPlayed(millis);
                        if(next.decreasing)
                            next.reminingTimes--;

                        boolean notOk = true;
                        if (next.reminingTimes <= 0)
                            Platform.runLater(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      timeline[loop].remove(next);

                                                  }
                                              }

                            );
                        else
                            timeline[loop].queue(next);
                        timeline[loop].getRythmPanel().textTimeline.setText("" + ((int)t*timeline[loop].getDuration()*100));

                    }
                }
            }
             catch (NullPointerException ex)
             {
                 System.out.println("Null");
             }
        }
    }

    private boolean isRunning() {
        return true;
    }

    public void pause() {
        pause = true;
    }
}
