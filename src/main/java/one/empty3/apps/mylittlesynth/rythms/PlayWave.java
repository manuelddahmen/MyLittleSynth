package one.empty3.apps.mylittlesynth.rythms;

import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class PlayWave extends Thread {
   private static int BUFFER_SIZE_WAV = 2000;
   private final AudioInputStream audio;
   private final TimelineThread timelineThread;
   private final Timeline.Model model;
   private boolean isRunning = true;

   public PlayWave(Timeline.Model model, AudioInputStream audioInputStream, TimelineThread timelineThread) {
      this.timelineThread = timelineThread;
      this.audio = audioInputStream;
      this.model = model;
   }

   public void playWave(AudioInputStream audioInputStream) {
      if (audioInputStream == null) {
         isRunning = false;
         return;
      }
      isRunning = true;
      SourceDataLine sourceLine = null;
      try {
         sourceLine = AudioSystem.getSourceDataLine(audioInputStream.getFormat());
         sourceLine.open();
         sourceLine.start();
         int nBytesRead = 0;
         byte[] abData = new byte[BUFFER_SIZE_WAV];

         while(isRunning && nBytesRead != -1) {
            try {
               nBytesRead = audioInputStream.read(abData, 0, abData.length);
            } catch (IOException ex) {
               ex.printStackTrace();
               break;
            }

            if (nBytesRead > 0) {
               sourceLine.write(abData, 0, nBytesRead);
            }
         }

         if (sourceLine.isOpen()) {
            sourceLine.drain();
         }
      } catch (LineUnavailableException var8) {
         System.out.println("matching line is not available due to resource restrictions");
      } catch (SecurityException var9) {
         System.out.println("if a matching line is not available due to security restrictions");
      } catch (IllegalArgumentException var10) {
         System.out.println("if the system does not support at least one line matching the specified Line.Info object through any installed mixer");
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (sourceLine != null) {
            try {
               sourceLine.stop();
               sourceLine.close();
            } catch (Exception ignored) {}
         }
         try {
            audioInputStream.close();
         } catch (IOException ignored) {}
         isRunning = false;
      }

   }

   public void stopPlaying() {
      this.isRunning = false;
   }

   public void run() {
      playWave(this.audio);
   }

   public boolean isRunning() {
      return isRunning;
   }
}
