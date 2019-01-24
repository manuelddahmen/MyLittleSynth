package be.manudahmen.mylittlesynth;

public class NoteState {
   private Note note;
   private long totalTimeElapsed;
   private boolean isPlaying;

   public NoteState(Note note, long totalTimeElapsed, boolean isPlaying) {
      this.note = note;
      this.totalTimeElapsed = totalTimeElapsed;
      this.isPlaying = isPlaying;
   }

   public Note getNote() {
      return this.note;
   }

   public void setNote(Note note) {
      this.note = note;
   }

   public long getTotalTimeElapsed() {
      return this.totalTimeElapsed;
   }

   public void setTotalTimeElapsed(long totalTimeElapsed) {
      this.totalTimeElapsed = totalTimeElapsed;
   }

   public boolean isPlaying() {
      return this.isPlaying;
   }

   public void setPlaying(boolean playing) {
      this.isPlaying = playing;
   }
}
