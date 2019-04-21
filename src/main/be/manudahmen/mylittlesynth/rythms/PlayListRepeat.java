package be.manudahmen.mylittlesynth.rythms;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

public class PlayListRepeat extends ListView {
    private int lastRepeat = 100;
    private Timeline timeline;

   PlayListRepeat(Timeline timeline){
        this.timeline = timeline;
        editableProperty().setValue(true);
        editableProperty().setValue(true);
        setOnEditCommit(new EventHandler<EditEvent>() {
            @Override
            public void handle(EditEvent event) {
                timeline.times.get(event.getIndex() - 1)
                        .reminingTimes = Integer.parseInt(
                        (String)timeline.getRythmPanel().
                                playList2.getItems().get(event.getIndex()-1));
            }
        });
    }

    public void display()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                getItems().clear();
                getItems().add(new Text("remining loops"));
                timeline.times.forEach(model1 ->
                        {
                            getItems().add(model1.reminingTimes);
                        }
                );
            }
        });

    }


}
