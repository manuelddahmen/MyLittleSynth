package be.manudahmen.mylittlesynth.rythms;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Timeline {
   ArrayList<Model> model = new ArrayList<Model>();
    private RythmPanel panel;
    private ArrayList<Model> modelCurrent;
    private int index = 0;

    public ArrayList<Model> getTimes() {
      return model;
   }

   public Timeline(RythmPanel rythmPanel)

   {
       this.panel = rythmPanel;
   }
   public synchronized void addFileAtTimePC(Double time, File file) {
      this.model.add(new Model(time, file));
   }

   public void delete(Model model) {
   }

   public void deleteFromTo(Double time0, Double time1) {
   }

    public double getDuration() {
        return panel.timelineTimeSec();
    }

    public double getDurationPC() {
        return 1.0;
    }

    public void init()
    {
        modelCurrent = (ArrayList<Model>) model.clone();
    }

    public Model getNext() {
        index++;
        if(index<model.size())
            return modelCurrent.remove(index);
        else {
            init();
            if(modelCurrent.size()>0)
                return modelCurrent.remove(0);
            else
                return null;
        }
    }

    public void add(Model model) {
        addFileAtTimePC(model.timeOnTimeline, model.wave);
    }

    public void remove(Model model) {
        modelCurrent.remove(model);
    }

    public void queue(Model model) {
        addFileAtTimePC(model.timeOnTimeline, model.wave);

    }

    public RythmPanel getRythmPanel() {
        return panel;
    }

    public void hasPlayed(int millisThread) {
        this.hasPlayedMillis(System.nanoTime(), millisThread) ;
    }

    private void hasPlayedMillis(long nanoTime, int millisThread) {

    }

    public void setTextTimeOnTimeline(File file) {
        panel.textTimeline.setText(""+panel.loopTimer.getCurrentTimeOnLineSec()+" "+file.getName());
    }

    class Model {
        double timeOnTimeline;
        File wave;

        public Model(Double time, File file) {
            this.timeOnTimeline = time;
            this.wave = file;
        }
    }
}
