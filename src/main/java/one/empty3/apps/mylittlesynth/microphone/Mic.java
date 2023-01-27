package one.empty3.apps.mylittlesynth.microphone;

import javax.sound.sampled.*;

/**
 * Created by manue on 01-09-19.
 */
public class Mic {
    Line mic;
    public Mic () throws LineUnavailableException {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info: mixerInfos){
            Mixer m = AudioSystem.getMixer(info);
            Line.Info[] lineInfos = m.getSourceLineInfo();
            for (Line.Info lineInfo:lineInfos){
                System.out.println (info.getName()+"---"+lineInfo);
                Line line = m.getLine(lineInfo);
                System.out.println("\t-----"+line);
            }
            lineInfos = m.getTargetLineInfo();
            for (Line.Info lineInfo:lineInfos){
                System.out.println (m+"---"+lineInfo);
                Line line = null;
                try {
                    line = m.getLine(lineInfo);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
                if(("\t-----"+line).contains("MICROPHONE"))
                    mic = line;

            }

        }
    }

    public static void main(String[] args)
    {
        try {
            Mic mic = new Mic();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
