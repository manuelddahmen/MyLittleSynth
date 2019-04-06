package be.manudahmen.mylittlesynth;

public class Timer {
   private long timeStartSystemNanoSec = System.nanoTime();
   private long timePause = 0L;
   private long definitiveTime = -1L;

   public void pause() {
      this.timePause = System.nanoTime();
   }

   public void resume() {
      this.timePause = 0L;
   }

   public long getTotalTimeElapsedNanoSec() {
      return System.nanoTime() - this.timeStartSystemNanoSec;
   }

   public void stop() {
      this.definitiveTime = (long)((double)this.getTotalTimeElapsedNanoSec());
   }

   public long getDefinitiveTime() {
      return this.definitiveTime;
   }

   public long getInitTime() {
      return this.timeStartSystemNanoSec;
   }
}
