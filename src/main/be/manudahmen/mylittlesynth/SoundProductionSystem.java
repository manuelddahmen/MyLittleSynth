package be.manudahmen.mylittlesynth;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFileFormat.Type;

public class SoundProductionSystem {
   private ByteArrayInputStream audioInputStreamWave;
   private OutputStream newOutWavFile;
   private boolean ended;
   private File outWavFile;
   private AudioFormat af;
   byte[] buffer;
   AudioInputStream audioInputStream;
   private int buffIdx;
   private final int samplerate;
   private int buffLen;
   private SourceDataLine sdl;
   private final int BUFFMAXLENGTH;
   boolean bigEndian;
   boolean signed;
   int bits;
   int channels;
   double sampleRate;
   long durationInSample = 44100*60*5;

   public SourceDataLine getLine() {
      return this.sdl;
   }

   public OutputStream getNewOutWavFile() {
      this.outWavFile = this.getOutWavFile();

      try {
         return new FileOutputStream(this.outWavFile);
      } catch (FileNotFoundException var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public boolean isEnded() {
      return this.ended;
   }

   public SoundProductionSystem(float secondsFile) {
      this();
      this.outWavFile = this.getOutWavFile();
      this.buffer = new byte[31752000];
      this.af = new AudioFormat((float)this.sampleRate, this.bits, this.channels, this.signed, this.bigEndian);
   }

   public File getOutWavFile() {
      return new File("outputWave-" + System.nanoTime() + "record.wav");
   }

   public void writeWaveBuffer(byte[] data) {
      int j = 0;

      for(int i = this.buffLen; i < this.buffLen + data.length; ++i) {
         this.buffer[i] = data[j++];
      }

      this.buffLen += 4;
      if (this.buffLen >= 31752000 || this.isEnded()) {
         this.write();
      }

   }

   public void write() {
      try {
         byte[] buf2 = new byte[this.buffLen];
         ByteArrayInputStream bais = new ByteArrayInputStream(this.buffer);
         this.audioInputStream = new AudioInputStream(bais, this.af, (long)(this.buffLen / 4));
         AudioSystem.write(this.audioInputStream, Type.WAVE, this.outWavFile);
         this.audioInputStream.close();
      } catch (IOException var3) {
         var3.printStackTrace();
      }

      this.buffLen = 0;
      this.outWavFile = this.getOutWavFile();
   }

   public SoundProductionSystem() {
      this.buffIdx = 0;
      this.samplerate = 44100;
      this.buffLen = 400;
      this.BUFFMAXLENGTH = 31752000;
      this.bigEndian = false;
      this.signed = true;
      this.bits = 16;
      this.channels = 2;
      this.sampleRate = 44100.0D;
      this.af = new AudioFormat(44100.0F, 16, 2, true, false);

      try {
         this.sdl = AudioSystem.getSourceDataLine(this.af);
      } catch (LineUnavailableException var3) {
         var3.printStackTrace();
      }

      try {
         this.sdl.open();
      } catch (LineUnavailableException var2) {
         var2.printStackTrace();
      }

      this.sdl.start();
   }

   public float calculateNoteFrequency(float halfTone) {
      return (float)(440.0D * Math.pow(2.0D, 0.08333333333333333D * (double)halfTone));
   }

   public void end() {
      this.sdl.drain();
      this.sdl.stop();

      try {
         this.audioInputStream.close();
      } catch (IOException var2) {
         var2.printStackTrace();
      }

   }

   public int equiv(String noteAnglaise) {
      assert noteAnglaise != null;

      int tone = 0;
      noteAnglaise = noteAnglaise.toUpperCase();
      switch(noteAnglaise.charAt(0)) {
      case 'A':
         tone += 0;
         break;
      case 'B':
         tone += 2;
         break;
      case 'C':
         tone -= 9;
         break;
      case 'D':
         tone -= 7;
         break;
      case 'E':
         tone -= 5;
         break;
      case 'F':
         tone -= 4;
         break;
      case 'G':
         tone -= 2;
      }

      String toneStr = "";
      if (Character.isDigit(noteAnglaise.charAt(1))) {
         toneStr = toneStr + "" + noteAnglaise.charAt(1);
      }

      if (noteAnglaise.length() > 2 && Character.isDigit(noteAnglaise.charAt(2))) {
         toneStr = toneStr + "" + noteAnglaise.charAt(2);
      }

      tone += Integer.parseInt(toneStr) * 12 - 4;
      tone = (int)((double)tone + (noteAnglaise.charAt(noteAnglaise.length() - 1) == '#' ? 0.5D : 0.0D));
      tone = (int)((double)tone + (noteAnglaise.charAt(noteAnglaise.length() - 1) == 'b' ? 0.5D : 0.0D));
      return tone;
   }

   public byte[] getBuffer() {
      return this.buffer;
   }

   public void setEnded(boolean ended) {
      this.ended = ended;
   }
}
