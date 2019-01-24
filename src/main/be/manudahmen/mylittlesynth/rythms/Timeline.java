package be.manudahmen.mylittlesynth.rythms;

import java.io.File;
import java.util.HashMap;

public class Timeline {
   HashMap model = new HashMap();

   public HashMap getTimes() {
      return this.model;
   }

   public void add(Double time, File file) {
      this.model.put(time, file);
   }

   public void delete(Double time) {
   }

   public void deleteFromTo(Double time0, Double time1) {
   }

   class Model {
      Double time;
      File file;
   }
}
