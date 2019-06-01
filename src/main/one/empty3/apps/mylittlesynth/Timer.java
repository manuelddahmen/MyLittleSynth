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
      return getTotalTimeElapsedMillisSec()/1000.;
   }

   public long getTotalTimeElapsedMillisSec() {
      return System.currentTimeMillis() - this.timeStartSystemNanoSec;
   }

   public void stop() {

         this.definitiveTimeMilli = this.getTotalTimeElapsedMillisSec();
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
