package one.empty3.apps.mylittlesynth.processor;

import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine.Info;

public class OutputProcessToLine extends Processor {
   private final OutputProcessToLine.ManagerBuffer managerBuffer;
   boolean isBigEndian = false;
   boolean isSigned = true;
   int bits = 16;
   int channels = 1;
   int sampleRate = 44100;
   long offset = 0L;
   private double amplitudeMax;
   private byte[] nextBuffer;
   private AudioFormat af;
   private SourceDataLine sdl;

   public OutputProcessToLine(double amplitudeMax) {
      this.amplitudeMax = amplitudeMax;
      this.managerBuffer = new OutputProcessToLine.ManagerBuffer();
      this.nextBuffer = new byte[this.channels * 2];
      System.out.println("output start");
   }

   public void run() {
      this.af = new AudioFormat((float)this.sampleRate, this.bits, this.channels, this.isSigned, this.isBigEndian);

      try {
         Info info = new Info(SourceDataLine.class, this.af);
         this.sdl = (SourceDataLine)AudioSystem.getLine(info);
         this.sdl.open(this.af, this.MAX_VALUES_SIZE * 16);
      } catch (LineUnavailableException var9) {
         System.out.println(var9.getMessage());
      }

      this.sdl.start();

      while(this.isRunning()) {
         long samplesCount = 0L;

         while(this.values.size() > 0 && samplesCount < 400L) {
            double v = (Double)this.values.removeFirst();
            byte[] nullOrBufferToPlay = this.addToBufferMono(v);
            if (nullOrBufferToPlay != null) {
               this.offset += (long)nullOrBufferToPlay.length;

               try {
                  this.sdl.write(nullOrBufferToPlay, 0, nullOrBufferToPlay.length);
               } catch (Exception var7) {
                  var7.printStackTrace();
               }

               ++samplesCount;
               if (samplesCount % (long)this.sampleRate == 0L) {
                  System.out.println("output runs");
               }
            }
         }

         try {
            sleep(10L);
         } catch (InterruptedException var8) {
            var8.printStackTrace();
         }
      }

      this.end();
   }

   byte[] addToBufferMono(double v) {
      double aD = this.amplitudeMax * v;
      short a = (short)((int)aD);
      if (this.nextBuffer.length == 2) {
         this.nextBuffer[0] = (byte)(a & 255);
         this.nextBuffer[1] = (byte)((a & '\uff00') >> 8);
      }

      if (this.nextBuffer.length == 4) {
         this.nextBuffer[2] = (byte)(a & 255);
         this.nextBuffer[3] = (byte)((a & '\uff00') >> 8);
      }

      return this.managerBuffer.addToBuffer(this.nextBuffer);
   }

   public void end() {
      this.sdl.drain();
      this.sdl.stop();
   }

   class ManagerBuffer {
      byte[] buffer;
      int bfIdx;

      ManagerBuffer() {
         this.buffer = new byte[OutputProcessToLine.this.MAX_VALUES_SIZE];
         this.bfIdx = 0;
      }

      public byte[] addToBuffer(byte[] buffer4) {
         if (this.bfIdx >= this.buffer.length - buffer4.length) {
            byte[] copy = Arrays.copyOf(this.buffer, this.bfIdx);
            this.bfIdx = 0;
            return copy;
         } else {
            for(int i = 0; i < buffer4.length; ++i) {
               this.buffer[this.bfIdx++] = buffer4[i];
            }

            return null;
         }
      }
   }
}
