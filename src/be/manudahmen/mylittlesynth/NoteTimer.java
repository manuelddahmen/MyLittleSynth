package be.manudahmen.mylittlesynth;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by win on 27/07/18.
 */
public class NoteTimer extends Timer {
    ArrayList<NoteState> notesRecorded = new ArrayList<>();

    public void add(NoteState noteState) {
        notesRecorded.add(noteState);
    }
}
