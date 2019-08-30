package one.empty3.apps.mylittlesynth.rythms;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

public class PlayListRepeat extends ListView {
    private int lastRepeat = 100;
    private Timeline[] timeline;
    private int loop;

    PlayListRepeat(Timeline[] timeline){
        this.timeline = timeline;
        editableProperty().setValue(true);
        editableProperty().setValue(true);
        setOnEditCommit(new EventHandler<EditEvent>() {
            @Override
            public void handle(EditEvent event) {
                timeline[loop].times.get(event.getIndex() - 1)
                        .reminingTimes = Integer.parseInt(
                        (String)timeline[loop].getRythmPanel().
                                playList2
                                .getItems().get(event.getIndex()-1));
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
                timeline[loop].times.forEach(model1 ->
                        {
                            getItems().add(model1.noLoop);
                        }
                );
            }
        });

    }


    public void setTimeline(Timeline[] timeline) {
        this.timeline = timeline;
    }

    public void setLoop(int loop) {
        this.loop = loop;
    }
}
