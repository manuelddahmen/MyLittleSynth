package one.empty3.apps.mylittlesynth;

import one.empty3.apps.mylittlesynth.processor.WaveForm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.application.Platform;

public class Player extends Thread {
   private final App app;
   private List notesRecorded;
   private final List noteStates;
   private List currentNotes;
   private Timer timer;
   private boolean playing;
   private Player that;
   private AudioViewer audioViewer;
   private int octave = 4;
   private String form;
   private WaveForm waveform;
   private double volume;
   private long position;
   private boolean recording;
   private boolean playingBuffer;
   private boolean loopPlayingBuffer;
   private NoteTimer timerRecording;
   double total;
   double facteurAmpl;
   Short a;
   byte[] nextBuffer;

   public List<NoteState> getNoteStates() {
      return this.noteStates;
   }

   public Player(App app, AudioViewer audioViewer) {
      this.waveform = WaveForm.SIN;
      this.volume = 100.0D;
      this.playingBuffer = false;
      this.loopPlayingBuffer = false;
      this.timerRecording = new NoteTimer();
      this.total = 0.0D;
      this.facteurAmpl = 0.0D;
      this.a = Short.valueOf((short)0);
      this.nextBuffer = new byte[4];
      this.app = app;
      this.currentNotes = Collections.synchronizedList(new ArrayList());
      this.noteStates = Collections.synchronizedList(new ArrayList());
      this.timer = new Timer();
      this.audioViewer = audioViewer;
      this.that = this;
      this.playing = true;
      this.position = 0L;
   }

   public void addNote(Note note) {
      this.currentNotes.add(note);
   }

   public void playCurrentNotes() {
      this.total = 0.0D;
      this.getCurrentNotes().forEach((note1) -> {
         Note note = (Note)note1;
         double noteTimeSec = (double)note.getTimer().getTotalTimeElapsedNanoSec() / 1.0E9D;
         double f2pi = (double)this.app.getSoundProductionSystem().calculateNoteFrequency((float)note.getTone()) * 2.0D * 3.141592653589793D;
         double f2piT = f2pi * noteTimeSec;
         this.facteurAmpl = note.getEnveloppe().getVolume(noteTimeSec);
         double ampl = 30000.0D * this.facteurAmpl;
         switch(note.getWaveform()) {
         case SIN:
            this.total += Math.sin(f2piT) * ampl;
            break;
         case RECT:
            this.total += Math.signum(Math.sin(f2piT)) * ampl;
         case SAWTOOTH:
            this.total += (1.0D - f2piT / 2.0D * 3.141592653589793D) * ampl;
         case TRI:
            this.total += 1.0D - Math.abs(f2piT / 2.0D * 3.141592653589793D) * ampl;
         default:
            this.total += Math.sin(f2piT) * ampl;
         }

         this.audioViewer.sendEnvelopeVolume(note.getTone(), note.getEnveloppe().getBrutVolume(noteTimeSec));
      });
      this.total /= this.currentNotes.size() > 0 ? (double)this.currentNotes.size() : 1.0D;
      short amplitude;
      if (this.getCurrentNotes().size() > 0) {
         amplitude = (short)((int)(this.total * this.volume / 100.0D));
         //this.audioViewer.sendDouble((double)amplitude * 1.0D);
         //this.audioViewer.sendDouble((double)amplitude * 1.0D);
         this.playBufferMono(amplitude);
         ++this.position;
      } else {
         amplitude = 0;
         //this.audioViewer.sendDouble(0.0D);
         //this.audioViewer.sendDouble(0.0D);
      }

   }

   public void playBufferMono(short amplitude) {
      this.nextBuffer[0] = (byte)(amplitude & 255);
      this.nextBuffer[1] = (byte)(amplitude >> 8);
      this.nextBuffer[2] = (byte)(amplitude & 255);
      this.nextBuffer[3] = (byte)(amplitude >> 8);

      try {
         this.app.getSoundProductionSystem().getLine().write(this.nextBuffer, 0, 4);
         this.app.getSoundProductionSystem().writeWaveBuffer(this.nextBuffer);
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   public void run() {
      while(this.isPlaying()) {
         this.playCurrentNotes();
      }

   }

   public boolean isPlaying() {
      return this.playing;
   }

   public void setPlaying(boolean playing) {
      this.playing = playing;
   }

   public Note addNote(int tone, long minDurationSec) {
      Note note = new Note(getApp().noteMap, (double)minDurationSec, tone, this.waveform, new Enveloppe((double)minDurationSec));
      Platform.runLater(() -> {
         this.getCurrentNotes().add(note);
         System.out.println("After added " + this.getCurrentNotes().size());
      });
      return note;
   }

   public void stopNote(int tone) {
      this.getCurrentNotes().forEach((note1) -> {
         Note note = (Note) note1;
         if (note.getTone() == tone) {
            Platform.runLater(() -> {
               this.getCurrentNotes().remove(note);
               System.out.println("After removed " + this.getCurrentNotes().size());
            });
         }

      });
   }

   public List getCurrentNotes() {
      return this.currentNotes;
   }

   public void setOctave(int octave) {
      this.octave = octave;
   }

   public int getOctave() {
      return this.octave;
   }

   public void setForm(String form) {
      this.form = form;
   }

   public void setWaveform(WaveForm waveform) {
      this.waveform = waveform;
   }

   public WaveForm getForm() {
      return this.waveform;
   }

   public void setVolume(double value) {
      this.volume = value;
   }

   public double getVolume() {
      return this.volume;
   }

   public void stopNote(Note note) {
      List notes = this.getCurrentNotes();
      synchronized(notes) {
         if (this.getCurrentNotes().contains(note)) {
            note.stop();
            notes.remove(note);
            if (this.isRecording()) {
               NoteState noteState = new NoteState(note, this.timerRecording.getTotalTimeElapsedNanoSec(), false);
               this.timerRecording.add(noteState);
            }
         }

      }
   }

   public void playNote(Note note) {
      Platform.runLater(() -> {
         if (!this.getCurrentNotes().contains(note)) {
            note.setWaveform(this.getForm());
            this.addNote(note);
            note.play();
            if (this.isRecording()) {
               NoteState noteState = new NoteState(note, this.timerRecording.getTotalTimeElapsedNanoSec(), true);
               this.timerRecording.add(noteState);
            }
         }

      });
   }

   public boolean isRecording() {
      return this.recording;
   }

   public void setRecording(boolean recording) {
      System.out.println("Recording: " + recording);
      this.recording = recording;
      if (this.isRecording()) {
         this.timerRecording.stop();
         this.setPlayingBuffer(true);
      } else {
         this.timerRecording.stop();
      }

   }

   private void playNoteBuffer(List notesRecorded) {
      (new RepeatThread(this) {
      }).start();
   }

   public void setPlayingBuffer(boolean playingBuffer) {
      this.playingBuffer = playingBuffer;
   }

   public boolean isPlayingBuffer() {
      return this.playingBuffer;
   }

   public void toggleRecording() {
      this.recording = !this.recording;
      if (!this.isRecording()) {
         this.setPlayingBuffer(true);
      }

      this.setRecording(this.recording);
   }

   public boolean isLoopPlayingBuffer() {
      return this.loopPlayingBuffer;
   }

   public void setLoopPlayingBuffer(boolean loopPlayingBuffer) {
      this.loopPlayingBuffer = loopPlayingBuffer;
   }

   public NoteTimer getTimerRecording() {
      return this.timerRecording;
   }

   public void setTimerRecording(NoteTimer timerRecording) {
      this.timerRecording = timerRecording;
   }

   public List getNotesRecorded() {
      return this.notesRecorded;
   }

   public App getApp() {
      return this.app;
   }
}
