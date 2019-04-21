package be.manudahmen.mylittlesynth.rythms;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

public class PlayList extends ListView {

    private Timeline timeline;

   PlayList(Timeline timeline){
        this.timeline = timeline;

    }

    public void display()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                getItems().clear();
                getItems().add(new Text("position/name"));
                timeline.times.forEach(model1 ->
                        {
                            getItems().add(new Text(model1.wave.getName()+"/"+(float)(timeline.getDuration()*model1.timeOnTimelinePC)));
                        }
                );
            }
        });

    }


}
