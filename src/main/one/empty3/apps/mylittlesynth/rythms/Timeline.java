package one.empty3.apps.mylittlesynth.rythms;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.function.Consumer;

public class Timeline {
    private int nbrLoops;
    private boolean decresing;
    private PlayList playList;
    List<Model> times = Collections.synchronizedList(new ArrayList<Model>());
    private RythmPanel panel;
    private int lastReminingLoops = 20;
    private PlayListRepeat playList2;
    private double newTime;
    private boolean isLoops;

    public Timeline(PlayList playList) {
        this.playList = playList;
    }
    
    public List<Model> getTimes() {
        return times;
    }
    
    Timeline(RythmPanel rythmPanel) {
        this.panel = rythmPanel;
    }

    private void sortList()
    {
        this.times.sort((o1, o2) -> {
            if (o1.timeOnTimelinePC >newTime&&o1.timeOnTimelinePC -newTime< o2.timeOnTimelinePC-newTime )
                return 1;
            else
                return -1;
        });
    }

    public synchronized void addFileAtTimePC(
            Double timePC, File file) {
        
        this.times.add(new Model(timePC, file));
        sortList();
        this.playList = panel.playList;
        this.playList2 = panel.playList2;
        playList.display();
        playList2.display();

    }
    
    public double getDuration() {
        return panel.timelineTimeSec();
    }
    
    public void add(Model model) {
        this.times.add(model);
    }
    
    public void remove(Model model) {
        this.times.remove(model);
        sortList();
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
        System.out.println("Samples' list size :" + times.size());
    }

    public synchronized Model next(double timeOnLinePC, Model prev) {
        if (newTime > timeOnLinePC) {
            if (isLoops()) {

                nbrLoops++;
                this.newTime = timeOnLinePC;
            }

        }

        final Model[] choice = new Model[1];


        if (times.size() > 0) {
            Model model = times.get(0);
            if (model.noLoop <= nbrLoops)

            {
                choice[0] = model;
                newTime = model.getTimeOnTimelinePC();
                model.noLoop++;
                getRythmPanel().writeNoLoop(nbrLoops);
            }

            return choice[0] != null && choice[0] != prev ? choice[0] : null;
        } else return null;
    }
    
    public synchronized void deleteAt(Double position) {
        boolean error = true;
        while (error) {
            try {
                {
                    times.forEach(model -> {
                        if (model.timeOnTimelinePC == position)
                            times.remove(model);
                    });
                }
                error = false;
            } catch (ConcurrentModificationException ex) {
            
            }
        }
        playList.display();
    }

    public void setLoops(boolean loops) {
        this.isLoops = loops;
    }

    public boolean isLoops() {
        return isLoops;
    }

    class Model {
        boolean decreasing;
        int noLoop;
        int reminingTimes;
        double timeOnTimelinePC;
        File wave;
        
        public Model(Double time, File file) {
            this.timeOnTimelinePC = time;
            this.wave = file;
            this.reminingTimes = lastReminingLoops;
            this.decreasing = Timeline.this.decresing;
            this.noLoop = 0;
            
        }
        
        public boolean isDecreasing() {
            return decreasing;
        }
        
        public void setDecreasing(boolean decreasing) {
            this.decreasing = decreasing;
        }
        
        public int getNbrLoops() {
            return nbrLoops;
        }
        
        public void setNbrLoops(int noLoop) {
            this.noLoop= noLoop;
        }
        
        public int getReminingTimes() {
            return reminingTimes;
        }
        
        public void setReminingTimes(int reminingTimes) {
            this.reminingTimes = reminingTimes;
        }
        
        public double getTimeOnTimelinePC() {
            return timeOnTimelinePC;
        }
        
        public void setTimeOnTimelinePC(double timeOnTimelinePC) {
            this.timeOnTimelinePC = timeOnTimelinePC;
        }
        
        public File getWave() {
            return wave;
        }
        
        public void setWave(File wave) {
            this.wave = wave;
        }
        
        public String toString() {
            return
                    wave.getName() + "|" + ((int) timeOnTimelinePC * 100)
                            + "|" + reminingTimes + " loops";
        }
    }
    
    public String toString() {
        String modelString = "";
        for (int i = 0; i < times.size(); i++)
            modelString += "||" + (times.get(i).timeOnTimelinePC) + " " + times.get(i).wave.getName() + "||";
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
