package be.manudahmen.mylittlesynth;

/**
 * Created by win on 27/07/18.
 */
public class NoteState {
    private Note note;
    private double totalTimeElapsed;
    private boolean isPlaying;

    public NoteState(Note note, double totalTimeElapsed, boolean isPlaying) {
        this.note = note;
        this.totalTimeElapsed = totalTimeElapsed;
        this.isPlaying = isPlaying;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public double getTotalTimeElapsed() {
        return totalTimeElapsed;
    }

    public void setTotalTimeElapsed(double totalTimeElapsed) {
        this.totalTimeElapsed = totalTimeElapsed;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}
