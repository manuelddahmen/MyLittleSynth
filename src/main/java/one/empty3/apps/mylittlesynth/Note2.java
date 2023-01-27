package one.empty3.apps.mylittlesynth;

import one.empty3.apps.mylittlesynth.processor.WaveForm;

/**
 * Created by manue on 10-10-19.
 */
public class Note2 extends Note {
    private int tone;
    private int instrument;

    public Note2(int tone, int instrument) {
        super(null, 5.0, tone, WaveForm.SIN);
        this.tone = tone;
        this.instrument = instrument;
    }


    public int getTone() {
        return tone;
    }

    public void setTone(int tone) {
        this.tone = tone;
    }

    public int getInstrument() {
        return instrument;
    }

    public void setInstrument(int instrument) {
        this.instrument = instrument;
    }
}
