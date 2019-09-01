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
                timeline[loop].models.get(event.getIndex() - 1)
                        .reminingTimes = Integer.parseInt(
                        (String)timeline[loop].getRythmPanel().
                                playListRepeat
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
                getItems().add(new Text("#playing"));
                timeline[loop].models.forEach(model1 ->
                        {
                            getItems().add("NoLoop : "+model1.noLoop+" ; NoPlaying : " + model1.noPlaying);
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
