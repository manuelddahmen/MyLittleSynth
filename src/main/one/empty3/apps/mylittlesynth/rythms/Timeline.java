package one.empty3.apps.mylittlesynth.rythms;


import java.io.File;
import java.util.*;

public class Timeline {
    public static final double MIN_DELAY = 0.5;
    int track;
    private boolean newLoop = true;
    private int nbrLoops;
    private boolean decresing;
    private PlayList playList;
    LinkedList<Model> models = new LinkedList<Model>();
    private RythmPanel panel;
    private PlayListRepeat playList2;
    private double newTime;
    private boolean isLoops;
    private int currentItem;
    public double volume = 1.0;

    public List<Model> getModels() {
        return models;
    }
    
    Timeline(RythmPanel rythmPanel, int track) {
        this.panel = rythmPanel;
        this.track = track;
    }


    public synchronized void addFileAtTimePC(
            Double timePC, File file) {
        
        this.models.add(new Model(file));
    }
    
    public double getDuration() {
        return panel.timelineTimeSec(track);
    }
    
    public void add(Model model) {
        this.models.add(model);
    }
    
    public void remove(Model model) {
        this.models.remove(model);
        playList.display();
        playList2.display();
    }
    
    public void queue(Model model) {
        remove(model);
        add(model);
    }
    
    public RythmPanel getRythmPanel() {
        return panel;
    }
    
    
    
    public void setTextTimeOnTimeline(File file) {
        panel.textTimeline.setText(file.getName());
        System.out.println("Samples' list size :" + models.size());
    }

    public synchronized Model next() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Model choice = null;
        if (models.size() > 0 ) {
        for(int m = 0; m<models.size(); m++) {
            Model model = models.get(m);
                if (model != null && isPlayNow(getT(track), model))
                {
                    choice = model;
                    model.noLoop = nbrLoops;
                    break;
                }
            }
        }
        return choice;
    }
    public void deleteAt(int selectedIndex) {
        models.remove(selectedIndex);
    }

    public synchronized void deleteAt(Double position) {
        boolean error = true;
        while (error) {
            try {
                {
                    models.forEach(model -> {
                        if (model.timeOnTimeline == position)
                            models.remove(model);
                    });
                }
                error = false;
            } catch (ConcurrentModificationException ex) {
            
            }
        }
    }

    private boolean isPlayNow(double t, Timeline.Model next) {

        boolean cond1 = Math.abs(next.timeOnTimeline - getT(track)) < MIN_DELAY;
        boolean cond2 = Math.abs(next.lastPlay - getT(track)) > MIN_DELAY;
        if(cond1 && cond2)
        {
            next.lastPlay = getT(track);
            return true;
        }
        else {
            return false;
        }

    }



    private double getT(int track) {
        double t;
        t = panel.loopTimer[track].getCurrentTimeOnLineSec();
        return t;
    }
    public void setLoops(boolean loops) {
        this.isLoops = loops;
    }

    public boolean isLoops() {
        return isLoops;
    }


    class Model {
        int noPlaying = 0;
        boolean decreasing;
        int noLoop=0;
        int reminingTimes;
        double timeOnTimeline;
        File wave;
        int noTrack =0;
        double lastPlay = 0;
        
        public Model(File file) {
            this.timeOnTimeline = getRythmPanel().getLoopTimer()[track].getCurrentTimeOnLineSec();
            this.wave = file;
            this.decreasing = Timeline.this.decresing;
            this.noLoop = nbrLoops;
            this.noTrack = track;
            
        }
        
        public boolean isDecreasing() {
            return decreasing;
        }
        
        public void setDecreasing(boolean decreasing) {
            this.decreasing = decreasing;
        }

        
        public int getReminingTimes() {
            return reminingTimes;
        }
        
        public void setReminingTimes(int reminingTimes) {
            this.reminingTimes = reminingTimes;
        }
        
        public double getTimeOnTimelinePC() {
            return timeOnTimeline;
        }
        
        public void setTimeOnTimelinePC(double timeOnTimelinePC) {
            this.timeOnTimeline = timeOnTimelinePC;
        }
        
        public File getWave() {
            return wave;
        }
        
        public void setWave(File wave) {
            this.wave = wave;
        }
        
        public String toString() {
            return
                    wave.getName() + "|" + ((int) timeOnTimeline)
                            + "|" + reminingTimes + " loops";
        }
    }
    
    public String toString() {
        String modelString = "";
        for (int i = 0; i < models.size(); i++)
            modelString += "||" + (models.get(i).timeOnTimeline) + " " + models.get(i).wave.getName() + "||";
        return "timeline (duration:'" + getDuration() + modelString + "  )";
    }
    
    public int getNbrLoops() {
        return nbrLoops;
    }
    
    public void setNbrLoops(int nbrLoops) {
        this.nbrLoops = nbrLoops;
    }
    
    public boolean isDecresing() {
        return decresing;
    }
    
    public void setDecresing(boolean decresing) {
        this.decresing = decresing;
    }
}
