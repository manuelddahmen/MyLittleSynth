package be.manudahmen.mylittlesynth.rythms;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Timeline {
   List<Model> model = Collections.synchronizedList(new ArrayList<Model>());
    private RythmPanel panel;

    public List<Model> getTimes() {
      return model;
   }

   public Timeline(RythmPanel rythmPanel)

   {
       this.panel = rythmPanel;
   }
   public synchronized void addFileAtTimePC(Double timePC, File file) {
      this.model.add(new Model(timePC, file));
      this.model.sort((o1, o2) -> {
          if(o1.timeOnTimelinePC < o2.timeOnTimelinePC)
              return 1;
          else
              return -1;
      });
      System.out.println("add "+this.toString());
   }
    public double getDuration() {
        return panel.timelineTimeSec();
    }

    public void add(Model model) {
        this.model.add(model);
    }

    public void remove(Model model) {
        this.model.remove(model);
    }

    public void queue(Model model) {
        remove(model);
        add(model);

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
        panel.textTimeline.setText(file.getName());
        System.out.println("Samples' list size :" + model.size());
    }

    public Model next() {
        return model.size()>0?model.get(0):null;
    }

    class Model {
        double timeOnTimelinePC;
        File wave;

        public Model(Double time, File file) {
            this.timeOnTimelinePC = time;
            this.wave = file;
        }
    }

    public String toString()
    {
        String modelString= "";
        for(int i = 0; i<model.size(); i++)
            modelString += "||"+(model.get(i).timeOnTimelinePC*getDuration()) + " "+ model.get(i).wave.getName()+"||";
        return "timeline (duration:'"+getDuration()+modelString+ "  )";
    }
}
