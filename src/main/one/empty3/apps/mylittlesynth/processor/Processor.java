package one.empty3.apps.mylittlesynth.processor;

import java.util.HashMap;
import java.util.LinkedList;

public class Processor extends Thread {
   protected int MAX_VALUES_SIZE = 5512;
   protected LinkedList values = new LinkedList();
   HashMap<String,Processor> inputProcessors = new HashMap<String,Processor>();
   HashMap<String,Processor> outProcessors = new HashMap<String,Processor>();
   private boolean running = true;
   private HashMap properties = new HashMap();

   protected boolean hasExcess() {
      return this.values.size() > this.MAX_VALUES_SIZE;
   }

   public HashMap<String,Processor> getInputProcessors() {
      return this.inputProcessors;
   }

   public void setInputProcessors(HashMap<String,Processor> inputProcessors) {
      this.inputProcessors = inputProcessors;
   }

   public HashMap<String,Processor> getOutProcessors() {
      return this.outProcessors;
   }

   public void setOutProcessors(HashMap outProcessors) {
      this.outProcessors = outProcessors;
   }

   public void send(Processor p, double v) {
      p.queue(v);
   }

   public void queue(double v) {
      this.addToQueue(v);
   }

   private void addToQueue(double v) {
      this.values.add(v);
   }

   public void run() {
      long samplesCount = 0L;

      while(this.isRunning()) {
         if (this.values.size() > 0) {
            double toSend = (Double)this.values.removeFirst();
            this.outProcessors.forEach((s, processor) -> {
               if (!processor.hasExcess()) {
                  processor.queue(toSend);
               }

            });
         }

         if (samplesCount % 44100L == 0L) {
            try {
               Thread.sleep(10L);
            } catch (InterruptedException var5) {
               var5.printStackTrace();
            }
         }
      }

   }

   public void process() {
   }

   public boolean isRunning() {
      return this.running;
   }

   public void setRunning(boolean running) {
      this.running = running;
   }

   public LinkedList getValues() {
      return this.values;
   }
}
