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
        if (folder != null && folder.exists() && folder.isDirectory()) {
            if (panel != null && panel.model != null && loop >= 0 && loop < panel.model.length) {
                File dir = (currentDirectory != null && loop < currentDirectory.length && currentDirectory[loop] != null) ? currentDirectory[loop] : folder;
                panel.model[panel.loop] = new RythmModel(panel, dir);
            }
            getItems().clear();
            getItems().add(".");
            getItems().add("..");
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file != null) {
                        getItems().add(file.getName());
                    }
                }
            }
            setOnMouseClicked(event -> {
                Object selected = getSelectionModel().getSelectedItem();
                if (selected == null) {
                    return;
                }
                String file = selected.toString();
                if (file.equals(".")) {
                    listFiles();
                    return;
                } else if (file.equals("..")) {
                    if (currentDirectory != null && loop >= 0 && loop < currentDirectory.length && currentDirectory[loop] != null) {
                        currentDirectory[loop] = (new File(currentDirectory[loop].getAbsolutePath() + File.separator + ".."));
                    }
                    listFiles();
                    return;
                }

                if (currentDirectory != null && loop >= 0 && loop < currentDirectory.length && currentDirectory[loop] != null) {
                    String pathname = currentDirectory[loop].getAbsolutePath() + File.separator + file;
                    File targetFile = new File(pathname);
                    if (targetFile.exists() && targetFile.isDirectory()) {
                        currentDirectory[loop] = targetFile;
                        listFiles();
                        return;
                    }

                    if (file.endsWith(".wav") || file.endsWith(".mp3") || file.endsWith(".aif") || file.endsWith(".m4a") || file.endsWith(".mid") || file.endsWith("mid")) {
                        if (loopTimer != null && timeline != null && loop >= 0 && loop < timeline.length && timeline[loop] != null) {
                            double d = loopTimer.getCurrentTimeOnLineSec();
                            double dur = timeline[loop].getDuration();
                            if (dur > 0) {
                                timeline[loop].addFileAtTimePC(d / dur, targetFile);
                            }
                            System.out.println(d);
                        }
                    }
                }
            });
        } else {
            getItems().clear();
            getItems().add(".");
            getItems().add("..");
        }
    }
    public void listFiles()
    {
        if (currentDirectory != null && loop >= 0 && loop < currentDirectory.length && currentDirectory[loop] != null) {
            listFiles(currentDirectory[loop]);
        }
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
