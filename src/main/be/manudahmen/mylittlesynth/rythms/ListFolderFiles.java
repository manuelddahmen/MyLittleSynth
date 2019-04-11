package be.manudahmen.mylittlesynth.rythms;


import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.util.List;

public class ListFolderFiles extends ListView {
    private File currentDirectory;
    private TimelineThread timelineThread;
    private LoopTimer loopTimer;
    private RythmPanel panel;


    public ListFolderFiles(TimelineThread timelineThread, LoopTimer loopTimer)
    {
        this.timelineThread = timelineThread;
        this.loopTimer = loopTimer;
    }

    public void listFiles(File folder) {
        if (folder.isDirectory()) {
            getItems().clear();
            getItems().add(".");
            getItems().add("..");
            File[] files = folder.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                getItems().add(file.getName());
            }
            setOnMouseClicked(new EventHandler<MouseEvent>() {


                @Override
                public void handle(MouseEvent event) {
                    String file = getSelectionModel().getSelectedItem().toString();
                    if (file.equals(".")) {
                        listFiles();
                        return;
                    } else if (file.equals("..")) {
                        currentDirectory = (new File(currentDirectory.getAbsolutePath() + File.separator + ".."));
                        listFiles();
                        return;
                    }

                    String pathname = currentDirectory.getAbsolutePath() + File.separator + file;
                    if (new File(pathname).isDirectory()) {
                        currentDirectory = new File(pathname);
                        listFiles();
                        return;
                    }
                    if (file.endsWith(".wav")) {
                        double d = loopTimer.getCurrentTimeOnLineSec();
                        timelineThread.timeline.addFileAtTimePC(d / timelineThread.timeline.getDuration(), new File(pathname));
                        System.out.println(d);

                    }
                }
            });
        }
    }
    public void listFiles()
    {
        listFiles(currentDirectory);
    }
    public void setInitialDirectory(File file) {
        this.currentDirectory = file;
    }
}
