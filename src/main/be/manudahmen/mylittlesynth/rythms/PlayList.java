package be.manudahmen.mylittlesynth.rythms;

import javafx.scene.control.ListView;
import javafx.scene.text.Text;

public class PlayList extends ListView {

    private Timeline timeline;

   PlayList(Timeline timeline){
        this.timeline = timeline;

    }

    public void display()
    {
        getItems().clear();
        getItems().add(new Text("position/name"));
        timeline.times.forEach(model1 ->
        {
            getItems().add(new Text(model1.wave.getName()+"/"+model1.timeOnTimelinePC));
        }
        );

    }


}
