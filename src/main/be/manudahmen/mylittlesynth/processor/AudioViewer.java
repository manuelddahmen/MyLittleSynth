package be.manudahmen.mylittlesynth.processor;

import be.manudahmen.mylittlesynth.processor.apps.ApplicationPane;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.NoSuchElementException;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class AudioViewer extends Processor {
   private final ApplicationPane app;
   private int numberOfSamplesPerPixel = 1;
   private double zoom = 1.0D;
   private Canvas drawingZone;
   protected int channels;
   protected float sampleRate;
   protected double position;
   protected boolean running;
   private Point2D lastPoint2D;

   private List getDoubles() {
      return this.values;
   }

   public AudioViewer(float sampleRate, int channels, ApplicationPane applicationPane) {
      this.sampleRate = sampleRate;
      this.channels = channels;
      this.app = applicationPane;
      this.running = true;
      (new AudioViewer.CanvasRedrawTask()).start();
   }

   protected void render(long timeElapsed, int numberOfSamples) {
      if (this.app == null) {
         System.exit(-1);
      }

      this.drawingZone = this.app.getDrawingZone1();
      if (this.drawingZone == null) {
         System.exit(-2);
      }

      GraphicsContext context2D = this.drawingZone.getGraphicsContext2D();
      double maxHeight = this.drawingZone.getHeight() / 2.0D;
      double maxWidth = this.drawingZone.getWidth() * this.getZoom();
      int total = 0;
      if (this.values.size() > 1000) {
         int size = (int)Math.min(this.drawingZone.getWidth() * 2.0D * (double)this.numberOfSamplesPerPixel, (double)Math.min(this.values.size(), this.MAX_VALUES_SIZE));
         double[] samples = new double[size];

         int halfSize;
         do {
            halfSize = 0;
            double totalDouble = 0.0D;

            try {
               while(this.values.size() > 1 && halfSize < size) {
                  samples[halfSize] = (Double)this.values.removeFirst();
                  totalDouble += samples[halfSize];
                  ++total;
                  ++halfSize;
               }
            } catch (NoSuchElementException var20) {
               System.out.print(" NO SUCH ELEMENT EX");
            }
         } while(total < size);

         halfSize = total / 2;
         this.numberOfSamplesPerPixel = (int)this.zoom;
         int arrayLength = halfSize / (int)this.zoom;
         double[] xpoints = new double[arrayLength];
         double[] xs = new double[arrayLength];
         double[] ys1 = new double[arrayLength];
         double[] ys2 = new double[arrayLength];

         for(int i = 0; i < halfSize / (int)this.zoom; ++i) {
            xs[i] = (double)(this.position++);
            xpoints[i] = xs[i];
            int k = 0;
            ys1[i] = 0.0D;
            if (this.channels == 2) {
               ys2[i] = 0.0D;
            }

            while(k < this.numberOfSamplesPerPixel && i + k <= samples.length - this.channels) {
               ys1[i] += samples[i + k++];
               if (this.channels == 2) {
                  ys2[i] += samples[i + k++];
               }

               if (this.position >= this.drawingZone.getWidth()) {
                  this.position = 0.0D;
                  context2D.clearRect(0.0D, 0.0D, this.drawingZone.getWidth(), this.drawingZone.getHeight());
               }
            }

            ys1[i] = ys1[i] / (double)this.numberOfSamplesPerPixel * maxHeight + maxHeight;
            if (this.channels == 2) {
               ys2[i] = ys2[i] / (double)this.numberOfSamplesPerPixel * maxHeight + maxHeight;
            }
         }

         this.drawBorder(context2D, Color.RED);
         if (this.channels == 2) {
            context2D.setFill(Color.BLACK);
            context2D.setStroke(Color.RED);
            context2D.strokePolyline(xpoints, ys2, xs.length);
         }

         context2D.setFill(Color.BLACK);
         context2D.setStroke(Color.BLACK);
         context2D.strokePolyline(xpoints, ys1, xs.length);
         context2D.fillText("Sample numbers: " + this.values.size(), maxWidth / 2.0D, maxHeight / 2.0D);
         context2D.fillText("last value: ", maxWidth / 2.0D, maxHeight / 2.0D + 30.0D);
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
      this.values.add(value);
   }

   public double getZoom() {
      return this.zoom;
   }

   public void setZoom(double zoom) {
      this.zoom = zoom;
   }

   protected class CanvasRedrawTask extends AnimationTimer {
      long time = System.nanoTime();
      long f = System.nanoTime();

      public void handle(long now) {
         AudioViewer.this.render(this.f, AudioViewer.this.getDoubles().size() / 2);
         this.f = System.nanoTime() - this.time;
         this.time = System.nanoTime();
      }
   }
}
