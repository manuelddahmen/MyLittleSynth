package be.manudahmen.mylittlesynth.rythms;

import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat.Encoding;

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


      new AudioFormat(Encoding.PCM_SIGNED, 44100.0F, 16, 2, 4, -1.0F, true);
      Object var2 = null;


      isRunning = true;

      try {
         SourceDataLine sourceLine = AudioSystem.getSourceDataLine(audioInputStream.getFormat());
         sourceLine.open();
         sourceLine.start();
         int nBytesRead = 0;
         byte[] abData = new byte[BUFFER_SIZE_WAV];

         while(nBytesRead != -1) {
            try {
               nBytesRead = audioInputStream.read(abData, 0, abData.length);
            } catch (IOException var7) {
               var7.printStackTrace();
            }

            if (nBytesRead >= 0) {
               sourceLine.write(abData, 0, nBytesRead);
            }
         }

         sourceLine.drain();
         sourceLine.close();
      } catch (LineUnavailableException var8) {
         System.out.println("matching line is not available due to resource restrictions");
      } catch (SecurityException var9) {
         System.out.println("if a matching line is not available due to security restrictions");
      } catch (IllegalArgumentException var10) {
         System.out.println("if the system does not support at least one line matching the specified Line.Info object through any installed mixer");
      }

      isRunning = false;

   }

   public void run() {
      playWave(this.audio);
   }

   public boolean isRunning() {
      return isRunning;
   }
}
