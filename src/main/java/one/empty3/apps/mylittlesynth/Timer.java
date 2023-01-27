package one.empty3.apps.mylittlesynth;

public class Timer {
   private long timeStartSystemNanoSec = System.nanoTime();
   private long timeStartSystemMilliSec = System.currentTimeMillis();
   private long timePause = 0L;
   private long definitiveTime = -1L;
   private long definitiveTimeMilli;
   private long definitiveTimeNano;

   public void pause() {
      this.timePause = System.nanoTime();
   }

   public void resume() {
      this.timePause = 0L;
   }

   public long getTotalTimeElapsedNanoSec() {
      return System.nanoTime() - this.timeStartSystemNanoSec;
   }

   public double getTotalTimeElapsedSec() {
      return getTotalTimeElapsedNanoSec()/1000000000.;
   }

   public long getTotalTimeElapsedNanosSec() {
      return System.nanoTime() - this.timeStartSystemNanoSec;
   }

   public void stop() {

         this.definitiveTimeMilli = this.getTotalTimeElapsedNanoSec()/1000000;
      this.definitiveTimeNano = this.getTotalTimeElapsedNanoSec();
   }

   public long getDefinitiveTimeNano() {
      return this.definitiveTimeNano;
   }

   public long getDefinitiveTimeMillis() {
      return this.definitiveTimeMilli;
   }

   public long getInitTime() {
      return this.timeStartSystemNanoSec;
   }
}
