package be.manudahmen.mylittlesynth.rythms;

import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlayList extends ListView {

    private Timeline timeline;

   PlayList(){
        getChildren().addAll(new Node[]{new TextField("Position"), new TextField("name")});


    }

    public void display(List<Timeline.Model> model)
    {
        getChildren().clear();
        timeline.model.forEach(model1 -> getChildren().add(new TextField(model1.timeOnTimelinePC+ " "+ model1.wave.getName()+"")));
    }


}
