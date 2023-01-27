package one.empty3.apps.mylittlesynth.rythms;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * Created by manue on 01-09-19.
 */
public class ListViewModel extends ListView {
    private Timeline[] timeline;
    private Timeline.Model model;

    public ListViewModel(Timeline[] timeline){
        this.timeline = timeline;
    }

    public ListViewModel() {

    }

    public void display()
    {
        if(model!=null)
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                getItems().clear();
                getItems().add(new Text("sample"));
                getItems().add(new Text("Last play : "+model.lastPlay));
                getItems().add(new Text("Track : "+model.noTrack));
                getItems().add(new Text("File : "+model.wave.getName()));
                getItems().add(new Text("#playing : "+model.noPlaying));
                Button delete = new Button("Delete");
                delete.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        timeline[model.noTrack].models.remove(model);
                        getItems().clear();
                    }
                });
                getItems().add(delete);
                TextField textField = new TextField(""+model.timeOnTimeline);
                textField.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        model.timeOnTimeline = Double.parseDouble(((TextField)event.getSource()).getText());
                    }
                });
                getItems().add(textField);

            }
        });

    }


    public void setTimeline(Timeline[] timeline) {
        this.timeline = timeline;
    }


    public void setModel(Timeline.Model model) {
        this.model = model;
        display();
    }
}
