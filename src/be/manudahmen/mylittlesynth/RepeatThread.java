package be.manudahmen.mylittlesynth;

import javafx.scene.control.Button;

/**
 * Created by win on 27/07/18.
 */
public class RepeatThread extends Thread {
    private Player player = null;

    public RepeatThread(Player player) {
        this.player = player;
        long timeStart = System.nanoTime()
                - player.getTimerRecording()
                .getInitTime();

    }


    public void run() {
        while (player.isPlayingBuffer()) {
            long current =
                    (System.nanoTime()
                            - player.getTimerRecording().getInitTime());
            player.getNotesRecorded().forEach(noteState -> {
                synchronized (player.getNoteStates()) {

                    if (noteState.getTotalTimeElapsed() > current && !noteState.isPlaying()) {
                        Note note = noteState.getNote();
                        note.setFinish(false);
                        noteState.setPlaying(true);
                        player.playNote(note);
                        Button button = player.getApp().getButton(note.getTone());
                        player.getApp().playNote(button);

                    } else if (noteState.getTotalTimeElapsed()
                            > current &&
                            !noteState.isPlaying()) {

                        Note note = noteState.getNote();
                        noteState.setPlaying(true);
                        note.setFinish(true);
                        Button button = player.getApp().getButton(note.getTone());
                        player.getApp().stopNote(button);
                        player.getNoteStates().remove(noteState);

                    }
                }
            });
        }
    }
}
