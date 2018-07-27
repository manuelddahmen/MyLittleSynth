package be.manudahmen.mylittlesynth;

import java.util.*;

/**
 * Created by win on 27/07/18.
 */
public class NoteTimer extends Timer {
    List<NoteState> notesRecorded = Collections.synchronizedList(new ArrayList<>());

    public void add(NoteState noteState) {
        notesRecorded.add(noteState);
    }

    public List<NoteState> getNotesRecorded() {
        return notesRecorded;
    }
}
