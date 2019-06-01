package one.empty3.apps.mylittlesynth.rythms;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class LoopProgress extends Canvas {
    
    private RythmPanel panel;
    
    public LoopProgress(RythmPanel rythmPanel)
    {
        
        super();
        this.panel = rythmPanel;
        setWidth(rythmPanel.getWidth());
        setHeight(50);
        new CanvasRedrawTask().start();
    }
    
    class CanvasRedrawTask extends AnimationTimer {
        public void handle(long now) {
            double width = getWidth();
            double height = getHeight();
            GraphicsContext g = getGraphicsContext2D();
            g.setFill(Color.GRAY);
            g.fill();
            int size = RythmPanel.size;
            for(int i = 0; i< size; i++)
            {
                double v = i * panel.loopTimer[panel.loop].getCurrentTimeOnLineSec() / panel.timelineTimeSec();
                if(v>1.0*i/size&&v<1.0*(i+1)/size) {
                    double left = 1.0 * i / size * width;
                    double right = 1.0 * i / size * height;
                    g.setFill(Color.BLACK);
                    g.rect(left, 0, right, height);
                }
            }
        }
    }
}
