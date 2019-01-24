package be.manudahmen.mylittlesynth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoteTimer extends Timer {
   List notesRecorded = Collections.synchronizedList(new ArrayList());

   public void add(NoteState noteState) {
      this.notesRecorded.add(noteState);
   }

   public List getNotesRecorded() {
      return this.notesRecorded;
   }
}
