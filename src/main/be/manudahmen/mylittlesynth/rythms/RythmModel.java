package be.manudahmen.mylittlesynth.rythms;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.io.File;
import java.util.ArrayList;

public class RythmModel  {
    Button[] buttons;
    ArrayList<File> files = new ArrayList<>();
    RythmPanel panel ;
    public RythmModel(RythmPanel panel, File rythmFiles) {
        this.panel = panel;
    }
    public void init()
    {

        int i = 0;
        buttons = new Button[panel.buttonsCount];
        int count = buttons.length;

        panel.getChildren().clear();
        for(int var10 = 0; var10 < count; ++var10) {
            if(files.size()>i) {
               String filename = files.get(i).toString();
                buttons[i] = new Button(filename);

                i++;
            }
        }
        for(i = 0; i < panel.size; ++i) {
            for(int j = 0; j < panel.columnCount; ++j) {
                Node[] nodes = new Node[panel.columnCount];
                nodes[j] = buttons[i * panel.columnCount + j];
                panel.setConstraints(nodes[j], j, i);
                panel.getChildren().addAll(new Node[]{nodes[j]});
            }
        }
    }

    public synchronized void init(File directory) {
        String[] fileArray = directory.list();
        int var3 = fileArray.length;

        int var4;
        for(var4 = 0; var4 < var3; ++var4) {
            String s = fileArray[var4];
            files.add(new File(directory + "/" + s));
        }
        ListView source = panel.playList;

        for(int i = 0; i < Math.min(files.size(), panel.buttonsCount); ++i) {
            File file = (File)files.get(i);
            if (!file.exists()) {
                break;
            }

            if (file.getName().endsWith(".wav")||file.getName().endsWith(".mp3")||file.getName().endsWith(".aiff")) {
                String name = file.getName().toString();
                this.buttons[i].setText(name);

                this.buttons[i].setOnMouseClicked((mouseEvent) -> {
                    double d = panel.loopTimer.getCurrentTimeOnLineSec();
                    panel.timeline.addFileAtTimePC(d/panel.timelineTimeSec(), file);
                    System.out.println(d);

                });
            }

        }

    }
}