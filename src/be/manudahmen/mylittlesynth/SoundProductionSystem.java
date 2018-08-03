/*
 * This file is part of Plants-Growth-2
 *     Plants-Growth-2 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Plants-Growth-2 is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Plants-Growth-2.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of Plants-Growth-2
 *     Plants-Growth-2 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Plants-Growth-2 is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Plants-Growth-2.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of Plants-Growth-2
 *     Plants-Growth-2 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Plants-Growth-2 is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Plants-Growth-2.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of Plants-Growth-2
 *     Plants-Growth-2 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Plants-Growth-2 is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Plants-Growth-2.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of Plants-Growth-2
 *     Plants-Growth-2 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Plants-Growth-2 is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Plants-Growth-2.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of Plants-Growth-2
 *     Plants-Growth-2 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Plants-Growth-2 is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Plants-Growth-2.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of Plants-Growth-2
 *     Plants-Growth-2 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Plants-Growth-2 is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Plants-Growth-2.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.manudahmen.mylittlesynth;

import javax.sound.sampled.*;
import java.io.*;

public class SoundProductionSystem {


    private ByteArrayInputStream audioInputStreamWave;
    private OutputStream newOutWavFile;
    private boolean ended;

    public SourceDataLine getLine() {
        return sdl;
    }

    public OutputStream getNewOutWavFile() {
        outWavFile = getOutWavFile();
        try {
            return new FileOutputStream(outWavFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isEnded() {
        return ended;
    }

    public enum Waveform {SIN, RECT, SAWTOOTH, TRI}

    ;
    private File outWavFile;
    private AudioFormat af;
    byte[] buffer;
    AudioInputStream audioInputStream;
    private int buffIdx = 0;
    private final int samplerate = 44100;
    private int buffLen = 100 * 4;
    private SourceDataLine sdl;
    private final int BUFFMAXLENGTH = 3 * 44100 * 60 * 4;
    boolean bigEndian = false;
    boolean signed = true;
    int bits = 16;
    int channels = 2;
    double sampleRate = 44100.0;

    public SoundProductionSystem(float secondsFile) {
        this();
        this.outWavFile = getOutWavFile();
        buffer = new byte[BUFFMAXLENGTH];
        af = new AudioFormat((float) sampleRate, bits, channels, signed, bigEndian);
    }

    public File getOutWavFile() {
        return
                new File
                        (
                                "outputWave-" + System.nanoTime() + "record.wav"
                        );
    }

    public void writeWaveBuffer(byte[] data) {
        int j = 0;
        for (int i = buffLen; i < buffLen + data.length; i++) {
            buffer[i] = data[j++];
        }
        buffLen += 4;
        if (buffLen >= BUFFMAXLENGTH || isEnded()) {
            write();
        }
    }

    public void write() {
        try {
            byte[] buf2 = new byte[buffLen];
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            audioInputStream = new AudioInputStream(bais, af, buffLen / 4);
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outWavFile);
            audioInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        buffLen = 0;
        outWavFile = getOutWavFile();
    }

    public SoundProductionSystem() {
        af = new AudioFormat((float) 44100, 16, 2, true, false);
        try {
            sdl = AudioSystem.getSourceDataLine(af);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        try {
            sdl.open();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        sdl.start();

    }

    public float calculateNoteFrequency(float halfTone) {
        return /*A*/ (float) (440 * Math.pow(2, 1 / 12.0 * halfTone));
    }


    public void end() {
        sdl.drain();
        sdl.stop();
        try {
            audioInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int equiv(String noteAnglaise) {
        assert noteAnglaise != null;
        int tone = 0;
        noteAnglaise = noteAnglaise.toUpperCase();
        switch (noteAnglaise.charAt(0)) {
            case 'C'://DO
                tone += 3 - 12;
                break;
            case 'D'://RE
                tone += 5 - 12;
                break;
            case 'E'://MI
                tone += 7 - 12;
                break;
            case 'F'://FA
                tone += 8 - 12;
                break;
            case 'G'://SOL
                tone += 10 - 12;
                break;
            case 'A'://LA
                tone += 0;
                break;
            case 'B'://SI
                tone += 2;
                break;
        }


        String toneStr = "";
        if (Character.isDigit(noteAnglaise.charAt(1)))
            toneStr += "" + noteAnglaise.charAt(1);
        if (noteAnglaise.length() > 2 &&
                Character.isDigit(noteAnglaise.charAt(2)))
            toneStr += "" + noteAnglaise.charAt(2);


        tone += ((Integer.parseInt(toneStr) * 12
                - 4))
        ;

        tone += (noteAnglaise.charAt(noteAnglaise.length() - 1) == '#') ? 0.5 : 0;
        tone += (noteAnglaise.charAt(noteAnglaise.length() - 1) == 'b') ? 0.5 : 0;

        return tone;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }
}
