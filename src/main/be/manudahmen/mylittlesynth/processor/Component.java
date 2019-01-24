package be.manudahmen.mylittlesynth.processor;

import java.util.HashMap;

public abstract class Component extends Processor {
   private HashMap componentProcessors = new HashMap();

   public Component() {
      this.initComponent();
   }

   public abstract void run();

   protected abstract void initComponent();

   private Processor getComponentProcessor(String name) {
      return (Processor)this.componentProcessors.get(name);
   }
}
