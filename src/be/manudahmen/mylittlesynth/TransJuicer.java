package be.manudahmen.mylittlesynth;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.File;
import java.util.HashMap;

public class TransJuicer extends Thread {
    private long timeElapsedNs = 0;
    private long nanoTimeBeginning;
    private HashMap<Long, File> inFile = new HashMap<>();
    private HashMap<Long, SourceDataLine> inLine = new HashMap<>();
    private File outFile;
    private TargetDataLine outLine;

    private int BUFLENGTH = 16;
    private double[] currentSamples = new double[BUFLENGTH];
    private boolean recording;

    public TransJuicer() {
        nanoTimeBeginning = System.nanoTime();
    }


    public void in(File file) {
        this.inFile.put(System.nanoTime() - nanoTimeBeginning, file);
    }

    public void in(SourceDataLine sourceDataLine) {
        this.inLine.put(System.nanoTime() - nanoTimeBeginning, sourceDataLine);

    }

    public void out(File file) {
        outFile = file;
    }

    public void out(TargetDataLine targetDataLine) {
        outLine = targetDataLine;
    }

    public void mix()

    {

    }

    public void run() {
        nanoTimeBeginning = System.nanoTime();


        while (isRecording()) {
            timeElapsedNs = System.nanoTime() - nanoTimeBeginning;


        }

    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }
}
