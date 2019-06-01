package one.empty3.apps.mylittlesynth.processor;

public class Mixer extends Processor {
   private final Option option;
   private double actualValue;

   public Mixer(Option option) {
      if (option == null) {
         option = Option.ADD;
      }

      this.option = option;
   }

   public void run() {
      while(this.isRunning()) {
         this.actualValue = 0.0D;
         if (this.values.size() > 0) {
            double v = 0.0D;
            this.getInputProcessors().forEach((s, processor) -> {
               Processor processor1 = (Processor) processor;
               if (this.option.equals(Option.ADD)) {
                  this.actualValue += (Double)processor.getValues().removeFirst();
               } else if (this.option.equals(Option.SUB)) {
                  this.actualValue -= (Double)processor.getValues().removeFirst();
               } else if (this.option.equals(Option.MULT)) {
                  this.actualValue += (Double)processor.getValues().removeFirst();
               } else if (this.option.equals(Option.SQRT_N)) {
                  this.actualValue *= (Double)processor.getValues().removeFirst();
               } else if (this.option.equals(Option.MEAN)) {
                  this.actualValue += (Double)processor.getValues().removeFirst();
               }

            });
            if (this.option.equals(Option.SQRT_N)) {
               this.actualValue = Math.pow(this.actualValue, 1.0D / (double)this.getInputProcessors().size());
            }

            if (this.option.equals(Option.MEAN)) {
               this.actualValue /= (double)this.getInputProcessors().size();
            }

            this.getOutProcessors().forEach((s, processor) -> {
               this.queue(this.actualValue);
            });
         }
      }

   }
}
