package one.empty3.apps.mylittlesynth;

import one.empty3.apps.mylittlesynth.processor.WaveForm;

import java.util.Map;

public class Note {
   private final Map instantiator;
   private int tone;
   private WaveForm waveform;
   //private Enveloppe enveloppe;
   private Timer timer;
   private double minDurationSec;
   private boolean finish;
   private long position;
   private long definitivePosition;
   private long noteTimeSamples = 0;
   public Note(Map instantiator, double minDurationSec, int tone, WaveForm waveform/*, Enveloppe enveloppe*/) {
      this.instantiator = instantiator;
      this.minDurationSec = minDurationSec;
      this.tone = tone;
      this.waveform = waveform;
      //this.enveloppe = enveloppe;
      this.position = 0L;
      this.init();
   }

   public double getMinDuration() {
      return this.minDurationSec;
   }

   public void setMinDuration(long minDuration) {
      this.minDurationSec = (double)minDuration;
   }

   public int getTone() {
      return this.tone;
   }

   public void setTone(int tone) {
      this.tone = tone;
   }

   public WaveForm getWaveform() {
      return this.waveform;
   }

   public void setWaveform(WaveForm waveform) {
      this.waveform = waveform;
   }
/*
   public Enveloppe getEnveloppe() {
      return this.enveloppe;
   }

   public void setEnveloppe(Enveloppe enveloppe) {
      this.enveloppe = enveloppe;
   }*/

   public void setTimer(Timer timer) {
      this.timer = timer;
   }

   public void init() {
      this.timer = new Timer();
     // this.enveloppe.setTimer(this.timer);
   }

   public Timer getTimer() {
      return this.timer;
   }

   public boolean isFinish() {

      boolean isFinshed = this.timer.getDefinitiveTimeNano() > this.timer.getTotalTimeElapsedNanoSec();

      if(isFinshed)
      {
         this.instantiator.remove(this);

      }
      return isFinshed;

   }

   public long getPosition() {
      this.position = System.nanoTime() - this.getTimer().getInitTime();
      return this.position;
   }

   public void play() {
      this.position = 0L;
   }

   public void stop() {
      this.definitivePosition = this.position;
      this.timer.stop();
   }

   public void positionInc() {
      ++this.position;
   }

   public void setFinish(boolean finish) {
      this.finish = finish;
   }

   public void setPosition(long position) {
      this.position = position;
   }

   public long getDefinitivePosition() {
      return this.definitivePosition;
   }

   public void setDefinitivePosition(long definitivePosition) {
      this.definitivePosition = definitivePosition;
   }

   public long getNoteTimeSamples() {
      return noteTimeSamples;
   }

   public void setNoteTimeSamples(long noteTimeSamples) {
      this.noteTimeSamples = noteTimeSamples;
   }
}
