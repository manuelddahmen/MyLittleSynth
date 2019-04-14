package be.manudahmen.mylittlesynth.rythms;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

public class Timeline {
    private PlayList playList;
    List<Model> times = Collections.synchronizedList(new ArrayList<Model>());
    private RythmPanel panel;

    public Timeline(PlayList playList)
    {
        this.playList = playList;
    }
    public List<Model> getTimes() {
      return times;
   }

   Timeline(RythmPanel rythmPanel)

   {

       this.panel = rythmPanel;
   }
   public synchronized void addFileAtTimePC(
           Double timePC, File file) {

      this.times.add(new Model(timePC, file));
      this.times.sort((o1, o2) -> {
          if(o1.timeOnTimelinePC > o2.timeOnTimelinePC)
              return 1;
          else
              return -1;
      });
       this.playList = panel.playList;
       playList.display();
      System.out.println("add "+this.toString());
   }
    public double getDuration() {
        return panel.timelineTimeSec();
    }

    public void add(Model model) {
        this.times.add(model);
    }

    public void remove(Model model) {
        this.times.remove(model);
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
        System.out.println("Samples' list size :" + times.size());
    }

    public synchronized Model next() {
        return times.size()>0? times.get(0):null;
    }

    public synchronized void deleteAt(Double position) {
        boolean error = true;
        while(error)
            {
                try {
                    {
                        times.forEach(model -> {
                            if (model.timeOnTimelinePC == position)
                                times.remove(model);
                        });
                    }
                    error = false;
                }catch (ConcurrentModificationException ex) {

                }
            }
            playList.display();
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
        for(int i = 0; i< times.size(); i++)
            modelString += "||"+(times.get(i).timeOnTimelinePC*getDuration()) + " "+ times.get(i).wave.getName()+"||";
        return "timeline (duration:'"+getDuration()+modelString+ "  )";
    }
}
