package one.empty3.apps.mylittlesynth.rythms;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class LoopProgress extends Canvas {

    private final RythmPanel panel;

    public LoopProgress(RythmPanel rythmPanel) {
        super();
        this.panel = rythmPanel;
        setWidth(Math.max(200, rythmPanel.getWidth()));
        setHeight(50);
        new CanvasRedrawTask().start();
    }

    class CanvasRedrawTask extends AnimationTimer {
        @Override
        public void handle(long now) {
            if (!isVisible() || getScene() == null || getScene().getWindow() == null || !getScene().getWindow().isShowing()) {
                return;
            }
            if (getParent() != null && !getParent().isVisible()) {
                return;
            }
            double width = getWidth();
            double height = getHeight();
            if (width <= 0 || height <= 0) {
                return;
            }
            GraphicsContext g = getGraphicsContext2D();
            g.setFill(Color.GRAY);
            g.fillRect(0, 0, width, height);
            int size = RythmPanel.size;
            if (panel == null || panel.loopTimer == null || panel.loop < 0 || panel.loop >= panel.loopTimer.length || panel.loopTimer[panel.loop] == null) {
                return;
            }
            double timelineSec = panel.timelineTimeSec(panel.loop);
            if (timelineSec <= 0) {
                return;
            }
            double cellWidth = width / size;
            for (int i = 0; i < size; i++) {
                double v = i * panel.loopTimer[panel.loop].getCurrentTimeOnLineSec() / timelineSec;
                if (v > 1.0 * i / size && v < 1.0 * (i + 1) / size) {
                    double left = 1.0 * i / size * width;
                    g.setFill(Color.BLACK);
                    g.fillRect(left, 0, cellWidth, height);
                }
            }
        }
    }
}
