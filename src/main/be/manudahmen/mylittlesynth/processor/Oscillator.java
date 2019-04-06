package be.manudahmen.mylittlesynth.processor;

import be.manudahmen.mylittlesynth.NoteTimer;
import be.manudahmen.mylittlesynth.Timer;

public class Oscillator extends Processor {
   private final NoteTimer timer;
   private final WaveForm waveForm;
   private final double amplitude;
   private double frequency;
   private boolean running = true;

   public Oscillator(double frequency, double amplitude, WaveForm waveform) {
      this.frequency = frequency;
      this.timer = new NoteTimer();
      this.waveForm = waveform;
      this.amplitude = amplitude;
   }

   public void run() {
      double gap = 0.0D;
      int countSample = 0;
      double ecartSamples = 0.0D;

      while(this.isRunning()) {
         double noteTimeSec = (double)this.timer.getTotalTimeElapsedNanoSec() / 1.0E9D;
         gap += noteTimeSec;
         double f2pi = this.frequency * 2.0D * 3.141592653589793D;
         double f2piT = f2pi * noteTimeSec;
         double ampl = this.amplitude;
         double total = 0.0D;
         switch(this.waveForm) {
         case SIN:
            total = Math.sin(f2piT) * ampl;
            break;
         case RECT:
            total = Math.signum(Math.sin(f2piT)) * ampl;
            break;
         case SAWTOOTH:
            total = (1.0D - f2piT / 2.0D * 3.141592653589793D) * ampl;
            break;
         case TRI:
            total = 1.0D - Math.abs(f2piT / 2.0D * 3.141592653589793D) * ampl;
            break;
         default:
            total = Math.sin(f2piT) * ampl;
         }

         final double total1 = total;
         this.outProcessors.forEach((s, processor) -> {
            if (!processor.hasExcess()) {
               processor.queue(total1);
            }

         });
         ++countSample;
         double ecartTypeFrequency = 1.0D;
         double sampleSpeed = (double)countSample / gap;
         double excessSpeed = 1.0D / (this.frequency + ecartTypeFrequency) * 44100.0D;
         if (sampleSpeed > excessSpeed) {
            try {
               Thread.sleep((long)((int)((noteTimeSec - gap) / ecartSamples * 44100.0D)));
               countSample = 0;
               gap = 0.0D;
            } catch (InterruptedException var27) {
               var27.printStackTrace();
            }

            ecartSamples = 0.0D;
         }
      }

   }

   public boolean isRunning() {
      return this.running;
   }

   public void setRunning(boolean running) {
      this.running = running;
   }

   public double getFrequency() {
      return this.frequency;
   }

   public void setFrequency(double frequency) {
      this.frequency = frequency;
   }

   public Timer getTimer() {
      return this.timer;
   }

   public WaveForm getWaveForm() {
      return this.waveForm;
   }
}
