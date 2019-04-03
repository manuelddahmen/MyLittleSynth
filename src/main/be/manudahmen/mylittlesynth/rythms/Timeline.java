package be.manudahmen.mylittlesynth.rythms;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class Timeline {
   ConcurrentHashMap model = new ConcurrentHashMap<Double, File>();
    private RythmPanel panel;
    private ConcurrentHashMap<File, Double> played = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Double, File> getTimes() {
      return this.model;
   }

   public Timeline(RythmPanel rythmPanel)

   {
       this.panel = rythmPanel;
   }
   public synchronized void addFileAtTimePC(Double time, File file) {
      this.model.put(time, file);
   }

   public void delete(Double time) {
   }

   public void deleteFromTo(Double time0, Double time1) {
   }

    public double getDuration() {
        return panel.timelineTimeSec();
    }

    public double getDurationPC() {
        return 1.0;
    }

    public Double hasPlayed(File file) {
        return played.get(file)==null?-1000d:played.get(file);
    }

    public void setPlayed(File file) {
        played.remove(file);
        played.put(file, panel.loopTimer.getCurrentTimeOnLineSec());
    }
}
