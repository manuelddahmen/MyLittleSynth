package one.empty3.apps.mylittlesynth.rythms;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.io.File;

public class ListFolderFiles extends ListView {
    private File[] currentDirectory;
    private LoopTimer loopTimer;
    private RythmPanel panel;
    private Timeline[] timeline;
    private int loop;


    public ListFolderFiles(Timeline[] timeline, LoopTimer loopTimer, RythmPanel panel)
    {
        this.timeline = timeline;
        this.loopTimer = loopTimer;
        this.panel = panel;
        currentDirectory = new File[panel.size];
    }

    public void listFiles(File folder) {
        if (folder.isDirectory()) {
            panel.model[panel.loop] = new RythmModel(panel, currentDirectory[loop]);
            getItems().clear();
            getItems().add(".");
            getItems().add("..");
            File[] files = folder.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                getItems().add(file.getName());
            }
            setOnMouseClicked(event -> {
                String file;
                try{
                    file = getSelectionModel().getSelectedItem().toString();
                }
                catch (NullPointerException ex)
                {
                    System.out.print("NullPointerException");
                    return;
                }
                if (file.equals(".")) {
                    listFiles();
                    return;
                } else if (file.equals("..")) {
                    currentDirectory[loop] = (new File(currentDirectory[loop].getAbsolutePath() + File.separator + ".."));
                    listFiles();
                    return;
                }

                String pathname = currentDirectory[loop].getAbsolutePath() + File.separator + file;
                if (new File(pathname).isDirectory()) {
                    currentDirectory[loop] = new File(pathname);
                    listFiles();

                    return;
                }
                if (file.endsWith(".wav")||file.endsWith(".mp3")||file.endsWith(".aif")||file.endsWith(".mp4")||file.endsWith("mid")) {
                    double d = loopTimer.getCurrentTimeOnLineSec();
                    timeline[loop].addFileAtTimePC(d / timeline[loop].getDuration(), new File(pathname));
                    System.out.println(d);

                }
            });
        }
    }
    public void listFiles()
    {

        listFiles(currentDirectory[loop]);
    }
    public void setInitialDirectory(File file) {
        for(int i=0; i<panel.size; i++)
            this.currentDirectory[i] = file;
    }

    public void setTimeline(Timeline timelineThread) {
        this.timeline = timeline;
    }

    public void setLoop(int loop) {
        this.loop = loop;
    }
}
