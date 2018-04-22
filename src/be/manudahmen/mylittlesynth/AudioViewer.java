/*
 * This file is part of Plants-Growth-2
 *     Plants-Growth-2 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Plants-Growth-2 is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Plants-Growth-2.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.manudahmen.mylittlesynth;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AudioViewer extends Thread {
    private WaitForData waitForData = new WaitForData();

    class WaitForData extends Thread {
        private final LinkedList<Double> doubles;

        public WaitForData() {
            doubles = values;
        }

        public void run() {

        }

        public LinkedList<Double> getDoubles() {
            return doubles;
        }

        public void addDouble(Double value) {
            this.doubles.add(value);
        }

    }

    private final int channels;
    private final float sampleRate;
    private final Canvas canvas;
    private double[] oldValues;
    private double min = 0;
    private double max = Short.MAX_VALUE;
    private double position;
    private final LinkedList<Double> values;
    private boolean running;
    ExecutorService
            executorService = Executors.newSingleThreadExecutor();
    Future<?> drawShapesFuture;

    public AudioViewer(float sampleRate, int channels, Canvas canvas) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.canvas = canvas;
        oldValues = new double[channels];
        running = true;
        values = new LinkedList<>();
        waitForData = new WaitForData();
        waitForData.start();

        new CanvasRedrawTask().start();

    }

    void render() {

        GraphicsContext context2D = canvas.getGraphicsContext2D();
        drawBorder(canvas.getGraphicsContext2D(), Color.RED);
        Enveloppe enveloppe = new Enveloppe(1);
        double[] ypoints = new double[enveloppe.points.length];
        double[] xpoints = new double[enveloppe.points.length];
        double maxHeight = canvas.getHeight() / 2;
        for (int i = 0; i < enveloppe.points.length; i++) {

            double y = enveloppe.points[i].getY() * maxHeight;
            double z = enveloppe.points[i].getZ() * maxHeight;

            xpoints[i] = z;
            ypoints[i] = y;


        }
        context2D.strokePolyline(xpoints, ypoints, xpoints.length);
        LinkedList<Double> doubles2 = waitForData.getDoubles();
        if (doubles2.size() >= channels) {
            //System.out.println("drawn");
            int size = (int) Math.min(canvas.getWidth() * 2, doubles2.size());

            double[] objects1 = new double[size];

            int i = 0;


            while ((doubles2.size() > 0) && (doubles2.getFirst() != null) && (i < size)) {
                objects1[i++] = doubles2.getFirst();
                doubles2.removeFirst();

            }

            double[] xs = new double[size / 2];
            double[] ys1 = new double[size / 2];
            double[] ys2 = new double[size / 2];


            xpoints = new double[size / 2];
            for (; i < size; ) {

                xs[i / 2] = position++;

                xpoints[i] = xs[i] / max + maxHeight / 2;

                ys1[i / 2] = objects1[i++] / max + maxHeight / 2;

                ys2[i / 2] = objects1[i++] / max + maxHeight / 2;


                if (position >= canvas.getWidth()) {
                    position = 0;
                }
            }
            context2D.setFill(Color.BLACK);
            context2D.setStroke(Color.BLUE);
            context2D.setLineWidth(4.0);
            context2D.strokePolyline(xpoints, ys1, size / 2);
            context2D.strokePolyline(xpoints, ys2, size / 2);


            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void drawBorder(GraphicsContext g, Color selectedColor) {
        final double canvasWidth = g.getCanvas().getWidth();
        final double canvasHeight = g.getCanvas().getHeight();

        g.setStroke(selectedColor);
        g.setLineWidth(4);
        g.strokeRect(0, 0, canvasWidth, canvasHeight);

        //sets the color back to the currently selected ColorPicker color

    }

    public void sendDouble(Double value) {
        waitForData.addDouble(value);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    class CanvasRedrawTask extends AnimationTimer {
        long time = System.nanoTime();

        @Override
        public void handle(long now) {
            render();
            long f = (System.nanoTime() - time) / 1000 / 1000;
            System.out.println("Time since last redraw " + f + " ms");
            time = System.nanoTime();
        }
    }
}
