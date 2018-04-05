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

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;

public class AudioViewer extends Thread {
    private final int channels;
    private final float sampleRate;
    private final Canvas canvas;
    private double[] oldValues;
    private double min = 0;
    private double max = Short.MAX_VALUE;
    private double position;
    private LinkedList<Double> values;
    private boolean running;

    public AudioViewer(float sampleRate, int channels, Canvas canvas) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.canvas = canvas;
        oldValues = new double[]{0.0, 0.0};
        running = true;
        values = new LinkedList<>();

    }

    public void run() {

        while (isRunning()) {
            GraphicsContext context2D = canvas.getGraphicsContext2D();
            context2D.setFill(Color.BLACK);
            context2D.setStroke(Color.BLUE);
            context2D.setLineWidth(2.0);
            if (values.size() >= channels) {
                double maxHeight = canvas.getHeight() / 2;
                double pos0Y = maxHeight;
                double[] toDraw;
                for (int i = 0; i < channels; i++) {
                    double first = values.removeFirst();
                    toDraw = new double[]
                            {
                                    position,
                                    oldValues[i] / max * maxHeight + pos0Y,
                                    position + 1,
                                    first / max * maxHeight + pos0Y
                            };
                    context2D.strokeLine(toDraw[0], toDraw[1], toDraw[2], toDraw[3]);
                    oldValues[i] = first;

                }

                position++;

                if (position >= canvas.getWidth()) {
                    position = 0;
                    oldValues = new double[]{0.0, 0.0};
                    //System.out.println("+");
                }
            }
        }
    }

    public void sendDouble(Double values) {
        this.values.add(values);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
