package one.empty3.apps.mylittlesynth;

import javafx.scene.control.Button;

public class RepeatThread extends Thread {
   private PlayerSwing playerS;
   private Player player = null;

   public RepeatThread(Player player) {
      this.player = player;
      long timeStart = System.nanoTime() - player.getTimerRecording().getInitTime();
   }

   public RepeatThread(PlayerSwing player) {
      this.playerS = player;
      long timeStart = System.nanoTime() - player.getTimerRecording().getInitTime();
      //TODO: EXTRACT
   }
   public void run() {
      while(this.player.isPlayingBuffer()) {
         long current = System.nanoTime() - this.player.getTimerRecording().getInitTime();
         this.player.getNotesRecorded().forEach((noteState) -> {
            synchronized(this.player.getNoteStates()) {
               NoteState noteState1 = (NoteState) noteState;
               Note note;
               Button button;
               if (noteState1.getTotalTimeElapsed() > current && !noteState1.isPlaying()) {
                  note = noteState1.getNote();
                  note.setFinish(false);
                  noteState1.setPlaying(true);
                  this.player.playNote(note);
                  button = this.player.getApp().getButton(note.getTone());
                  this.player.getApp().playNote(button);
               } else if (noteState1.getTotalTimeElapsed() > current && !noteState1.isPlaying()) {
                  note = noteState1.getNote();
                  noteState1.setPlaying(true);
                  note.setFinish(true);
                  button = this.player.getApp().getButton(note.getTone());
                  this.player.getApp().stopNote(button);
                  this.player.getNoteStates().remove(noteState);
               }

            }
         });
      }

   }
}
