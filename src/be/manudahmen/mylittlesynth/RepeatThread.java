package be.manudahmen.mylittlesynth;

/**
 * Created by win on 27/07/18.
 */
public class RepeatThread extends Thread {
    private Player player = null;

    public RepeatThread(Player player) {
        this.player = player;
    }

    long timeStart = System.nanoTime()
            - player.getTimerRecording()
            .getInitTime();

    public void run() {
        while (player.isPlayingBuffer()) {
            long current =
                    (System.nanoTime()
                            - player.getTimerRecording().getInitTime());
            player.getNotesRecorded().forEach(noteState -> {
                synchronized (player.getNoteStates()) {

                    if (noteState.getTotalTimeElapsed() > current && !noteState.isPlaying()) {
                        Note note = noteState.getNote();
                        noteState.setPlaying(true);
                        player.addNote(note);
                        player.getNoteStates().add(noteState);

                    } else if (noteState.getTotalTimeElapsed() +
                            noteState.getNote().getMinDuration()
                            > current

                            && !noteState.isPlaying()) {
                        Note note = noteState.getNote();
                        noteState.setPlaying(true);
                        player.stopNote(note);
                        player.getNoteStates().remove(noteState);

                    }
                }
            });
        }
    }
}
