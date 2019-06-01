package one.empty3.apps.mylittlesynth;

import java.util.*;
import java.util.stream.Stream;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class AudioViewer {
   private int numberOfSamplesPerPixel = 1;
   private final LinkedList doubles = new LinkedList();
   private Map<Integer, List<Double>> volumeEnvelopes = Collections.synchronizedMap(new HashMap());
   private double zoom = 1.0D;
   private final int channels;
   private final float sampleRate;
   private final Canvas canvas;
   private double[] oldValues;
   private double min = 0.0D;
   private double max = 32767.0D;
   private double position;
   private final LinkedList values;
   private boolean running;

   private LinkedList getDoubles() {
      return this.doubles;
   }

   private void addDouble(Double value) {
      this.doubles.add(value);
   }

   public void sendEnvelopeVolume(int tone, double volume) {
      if (!this.volumeEnvelopes.containsKey(tone)) {
         this.volumeEnvelopes.put(tone, new ArrayList<Double>());
      }

      ((ArrayList)this.volumeEnvelopes.get(tone)).add(volume);
   }

   public AudioViewer(float sampleRate, int channels, Canvas canvas) {
      this.sampleRate = sampleRate;
      this.channels = channels;
      this.canvas = canvas;
      this.oldValues = new double[channels];
      this.running = true;
      this.values = new LinkedList();
      (new AudioViewer.CanvasRedrawTask()).start();
   }

   void render(long timeElapsed, int numberOfSamples) {
      GraphicsContext context2D = this.canvas.getGraphicsContext2D();
      int maxWidth = (int)this.canvas.getWidth();
      Enveloppe enveloppe = new Enveloppe((double)timeElapsed / 1.0E9D);
      double[] xPoints = new double[enveloppe.points.length];
      double[] yPoints = new double[enveloppe.points.length];
      double[] xPoints2 = new double[maxWidth];
      double[] yPoints2 = new double[maxWidth];
      double maxHeight = this.canvas.getHeight() / 2.0D;

      int i;
      double v;
      for(i = 0; i < enveloppe.points.length; ++i) {
         v = enveloppe.points[i].getY() * maxHeight;
         double z = enveloppe.points[i].getZ() * this.canvas.getWidth();
         xPoints[i] = z;
         yPoints[i] = -v + maxHeight;
      }

      for(i = 0; i < xPoints2.length; ++i) {
         v = enveloppe.getForm(1.0D * (double)i / (double)maxWidth);
         xPoints2[i] = (double)i;
         yPoints2[i] = -v * maxHeight + maxHeight;
      }

      LinkedList doubles2 = this.getDoubles();
      int total = 0;
      if (doubles2.size() >= this.channels) {
         int size = (int)Math.min(this.canvas.getWidth() * 2.0D * (double)this.numberOfSamplesPerPixel, (double)doubles2.size());
         double[] samples = new double[size];

         int halfSize;
         do {
            halfSize = 0;

            try {
               while(doubles2.size() > 0 && doubles2.getFirst() != null && halfSize < size) {
                  samples[halfSize++] = (Double)doubles2.getFirst();
                  doubles2.removeFirst();
                  ++total;
               }
            } catch (NoSuchElementException var27) {
               System.out.print(" NO SUCH ELEMENT EX");
            }
         } while(this.sampleSpeed(timeElapsed, numberOfSamples) > this.treatmentSpeed(timeElapsed, total) && doubles2.size() == 0);

         halfSize = total / 2;
         this.numberOfSamplesPerPixel = (int)this.zoom;
         int arrayLength = halfSize / (int)this.zoom;
         double[] xpoints = new double[arrayLength];
         double[] xs = new double[arrayLength];
         double[] ys1 = new double[arrayLength];
         double[] ys2 = new double[arrayLength];
         double[][] env = new double[this.volumeEnvelopes.size()][total / 2];

         for(i = 0; i < halfSize / (int)this.zoom; ++i) {
            xs[i] = (double)(this.position++);
            xpoints[i] = xs[i];
            int k = 0;
            ys1[i] = 0.0D;
            ys2[i] = 0.0D;

            while(k < this.numberOfSamplesPerPixel && i + k <= samples.length - this.channels) {
               ys1[i] += samples[i + k++];
               ys2[i] += samples[i + k++];
               if (this.position >= this.canvas.getWidth()) {
                  this.position = 0.0D;
                  context2D.clearRect(0.0D, 0.0D, this.canvas.getWidth(), this.canvas.getHeight());
               }
            }

            ys1[i] = ys1[i] / (double)this.numberOfSamplesPerPixel * maxHeight / this.max + maxHeight;
            ys2[i] = ys2[i] / (double)this.numberOfSamplesPerPixel * maxHeight / this.max + maxHeight;
         }

         this.volumeEnvelopes.forEach((integer, doubles) -> {
            Object[] doubles1 = doubles.toArray();
            double[] unboxed = Stream.of(doubles1).mapToDouble((value) -> {
               return (Double)value;
            }).toArray();
            context2D.setFill(Color.BLACK);
            context2D.setStroke(Color.GREEN);
         });
         context2D.setLineWidth(1.0D);
         this.drawBorder(this.canvas.getGraphicsContext2D(), Color.RED);
         context2D.strokePolyline(xPoints, yPoints, xPoints.length);
         context2D.setFill(Color.BLACK);
         context2D.setStroke(Color.BLUE);
         context2D.strokePolyline(xPoints2, yPoints2, xPoints2.length);
         context2D.setFill(Color.BLACK);
         context2D.setStroke(Color.BLUE);
         context2D.strokePolyline(xpoints, ys1, Math.min(ys1.length, xpoints.length));
         context2D.strokePolyline(xpoints, ys2, Math.min(ys2.length, xpoints.length));

         try {
            Thread.sleep(10L);
         } catch (InterruptedException var26) {
            var26.printStackTrace();
         }
      }

   }

   private double treatmentSpeed(long time, int sample) {
      return 1.0D * (double)sample / (double)time;
   }

   private double sampleSpeed(long time, int sample) {
      return 1.0D * (double)sample / (double)time;
   }

   private void drawBorder(GraphicsContext g, Color selectedColor) {
      double canvasWidth = g.getCanvas().getWidth();
      double canvasHeight = g.getCanvas().getHeight();
      g.setStroke(selectedColor);
      g.setLineWidth(4.0D);
      g.strokeRect(0.0D, 0.0D, canvasWidth, canvasHeight);
   }

   public void sendDouble(Double value) {
      this.addDouble(value);
   }

   public boolean isRunning() {
      return this.running;
   }

   public void setRunning(boolean running) {
      this.running = running;
   }

   public double getZoom() {
      return this.zoom;
   }

   public void setZoom(double zoom) {
      this.zoom = zoom;
   }

   class CanvasRedrawTask extends AnimationTimer {
      long time = System.nanoTime();
      long f = System.nanoTime();

      public void handle(long now) {
         AudioViewer.this.render(this.f, AudioViewer.this.getDoubles().size() / 2);
         this.f = System.nanoTime() - this.time;
         this.time = System.nanoTime();
      }
   }
}
