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

import java.util.ArrayList;
import java.util.List;

public class AudioViewer extends Thread {
    private final int channels;
    private final float sampleRate;
    private final Canvas canvas;
    private double[] oldValues;
    private double min = 0;
    private double max = Short.MAX_VALUE;
    private double position;
    private List<Double[]> values;
    private boolean running;

    public AudioViewer(float sampleRate, int channels, Canvas canvas) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.canvas = canvas;
        oldValues = new double[channels];
        running = true;
        values = new ArrayList<>();
    }

    public void run() {

        while (isRunning()) {
            if (values.size() > 0) {

                Double[] newValues = values.get(0);
                values.remove(0);


                GraphicsContext graphicsContext2D = canvas.getGraphicsContext2D();
                double maxHeight = canvas.getHeight() / 2;
                double pos0Y = maxHeight;

                for (int i = 0; i < channels; i++) {
                    double[] toDraw = new double[]
                            {
                                    position,
                                    oldValues[i] / max * maxHeight + pos0Y,
                                    position,
                                    newValues[i] / max * maxHeight + pos0Y
                            };
                    double colorRatio = Math.random();
                    graphicsContext2D.setStroke(Color.color(colorRatio, colorRatio, colorRatio));
                    graphicsContext2D.strokeLine(toDraw[0], toDraw[1],
                            toDraw[2], toDraw[3]);
                }
                position++;

                if (position >= canvas.getWidth()) {
                    position = canvas.getHeight() / 2.0;
                    oldValues = new double[channels];
                    GraphicsContext graphicsContext2D1 = canvas.getGraphicsContext2D();
                    graphicsContext2D1.setFill(Color.WHITE);
                    graphicsContext2D1.fill();
                }
            }
        }
    }

    public void sendDouble(Double[] values) {
        this.values.add(values);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
