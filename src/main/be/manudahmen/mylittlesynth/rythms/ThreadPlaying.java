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

   public void run() {
      double [] time0 = new double[] {timer.getLoopDurationSec()};
      double[] time1 = new double[]{0.0D};

      while(this.running) {
         time0[0] = time1[0];
         HashMap times1 = this.timeline.getTimes();
         Collections.synchronizedMap(times1).forEach(
                 (time3, file) -> {

            double time =(Double) time3;
            double time2 = time0[0];
            time1[0] = ThreadPlaying.this.timer.getTimeOnLine();
            if (time > time0[0] && time < time1[0]) {
               try {
                  (new PlayWave(AudioSystem.getAudioInputStream((File)file))).start();
               } catch (UnsupportedAudioFileException | IOException ex) {
                  ex.printStackTrace();
               }
            }

         });
      }

   }
}
