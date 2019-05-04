package be.manudahmen.mylittlesynth.rythms;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class PlayList extends ListView {

    private Timeline timeline;

   PlayList(Timeline timeline){
        this.timeline = timeline;
       setOnMouseClicked(new EventHandler<MouseEvent>() {

           @Override
           public void handle(MouseEvent event) {
               getSelectionModel().getSelectedItem();

           }
       });
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
                            getItems().add(new Text(model1.wave.getName()+"/"+model1.timeOnTimelinePC));
                        }
                );
            }
        });

    }


    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }
}
