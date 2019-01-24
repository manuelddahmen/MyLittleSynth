package be.manudahmen.mylittlesynth;

public class Timer {
   private long timeElapsed = 0L;
   private long timeElapsedSystem = 0L;
   private long timePause = 0L;
   private long definitiveTime = -1L;

   public void init() {
      this.timeElapsedSystem = System.nanoTime();
      this.timeElapsed = 0L;
      this.definitiveTime = -1L;
   }

   public void pause() {
      this.timePause = System.nanoTime();
   }

   public void resume() {
      this.timePause = 0L;
   }

   public long getTotalTimeElapsed() {
      long timeInter = System.nanoTime();
      if (this.timePause > 0L) {
         this.timeElapsed = timeInter - this.timePause + this.timeElapsedSystem;
      } else {
         if (this.definitiveTime > 0L) {
            return this.definitiveTime;
         }

         this.timeElapsed = timeInter - this.timeElapsedSystem;
      }

      return this.timeElapsed;
   }

   public void stop() {
      this.definitiveTime = (long)((double)this.getTotalTimeElapsed() * 1.0E9D);
   }

   public long getDefinitiveTime() {
      return this.definitiveTime;
   }

   public long getInitTime() {
      return this.timeElapsedSystem;
   }
}
