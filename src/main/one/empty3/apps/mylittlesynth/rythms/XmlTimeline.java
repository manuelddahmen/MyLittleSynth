package one.empty3.apps.mylittlesynth.rythms;

import com.thoughtworks.xstream.XStream;

import java.io.File;

public class XmlTimeline {
    private Timeline[] audioSamples;
    
    public Timeline[] getAudioSamples() {
        return audioSamples;
    }
    
    public void setAudioSamples(Timeline[] audioSamples) {
        this.audioSamples = audioSamples;
    }
    
    public void save() {
        XStream xstream = new XStream();
        xstream.alias("timeline", Timeline.class);
        
        System.out.println(xstream.toXML(this));
    }
    
    public void load(File loop) {
        XStream xstream = new XStream();
        xstream.fromXML(loop);
        
        
    }
}
