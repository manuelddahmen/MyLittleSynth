package one.empty3.apps.mylittlesynth;

/**
 * Created by manue on 10-10-19.
 */
public class NoteSelector {
    private AppNew app;

    public NoteSelector(AppNew appNew)
    {
        this.app = appNew;

    }

    public Note2 chooseNote(int tone)
    {
        return new Note2(tone, -1);
    }
}
