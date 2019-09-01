package one.empty3.apps.mylittlesynth.rythms;


import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import one.empty3.apps.mylittlesynth.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ResourceBundle;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import javax.swing.*;

public class RythmPanel extends GridPane {
    private final App app;
    TextField textTimeline = new TextField("Timeline");
    public int loop = 0;
    private int timelineSizeDefault = 16;
    private TextField nbrLoops;
    private double time0timiLoop[] = new double[timelineSizeDefault];
    private int[] loops = new int[timelineSizeDefault];
    double timelineTimeSec(int track) {
        return 60. / tempo[track];
    }
    
    static int size = 8;
    private double[] tempo = new double[timelineSizeDefault];
    int columnCount = 4;
    int buttonsCount = size * columnCount;
    RythmModel[] model;
    Timeline[] timeline = new Timeline[size];
    private LoopProgress gridPaneTime;
    private Button[] buttons = new Button[size];
    private TimelineThread timelineThread;
    private TextField tempoText;
    LoopTimer[] loopTimer = new LoopTimer[timelineSizeDefault];
    private GridPane paneLine;
    private ListFolderFiles listFolderFiles;
    PlayList playList;
    PlayListRepeat playList2;
    private boolean noRepeat;

    public boolean isNoRepeat() {
        return noRepeat;
    }

    public void setNoRepeat(boolean noRepeat) {
        this.noRepeat = noRepeat;
    }

    void setTempo(double tempoCand) {
        this.tempo[loop] = tempo[loop] > 0 ? tempoCand : 1;
    }
    
    public LoopProgress getCanvasTimelineProgress() {
        return this.gridPaneTime
                ;
    }
    
    public RythmPanel(TitledPane container, App app) {
        this.model = new RythmModel[size];
        for (int i = 0; i < size; i++)
            this.model[i] = new RythmModel(this, new File("rythmFiles"));
        this.app = app;
        init();
    }
    
    public void init() {
        // Init components;
        for (int i = 0; i < size; i++) {
            tempo[i] = 20;
            loopTimer[i] = new LoopTimer(this, i);
            timeline[i] = new Timeline(this, i);
        }this.setGridLinesVisible(true);
        
        
        for (int j = 0; j < size / columnCount; j++)
            for (int i = 0; i < columnCount; i++) {
                buttons[j * columnCount + i] = new Button("Loop°" + (j * columnCount + i));
                setConstraints(buttons[j * columnCount + i], i, j);
                buttons[j * columnCount + i].setOnMouseClicked(event -> {
                    String name = ((Button) event.getSource()).getText();
                    int loop2 = Integer.parseInt(name.split("°")[1]);
                    loadLoop(loop2);
                });
            }
        getChildren().addAll(buttons);
        LoopProgress progress = new LoopProgress(this);
        this.gridPaneTime = progress;
        setConstraints(progress, 0, 0);
        this.getChildren().addAll(progress);
        Slider slider = new Slider(0.1D, 20.0D, 1.0D);
        setConstraints(slider, 6, 0);
        this.getChildren().addAll(slider);
        slider.setValueChanging(true);

        tempoText = new TextField("" + tempo[loop]);
        tempoText.setOnAction(event -> {
            try {
                double tempoValueCandidate = Double.parseDouble(tempoText.getText());
                tempo[loop] = tempoValueCandidate > 0 ? tempoValueCandidate : 1;
                System.out.println("Tempo: " + tempo[loop]);
            }
            catch (Exception ignored) {
            
            }
        });
        tempoText.onScrollProperty().set(event -> {
            int sign = 0;
            if (event.getDeltaY() > 0) {
                sign = 1;
            } else if (event.getDeltaY() <= 0) {
                sign = -1;
            }
            setTempo(tempo[loop] + sign);
            tempoText.setText("" + tempo[loop]);
        });
        this.getChildren().add(tempoText);
        setConstraints(tempoText, 0, 10);
        
        
        // Init loops
        
        

        
        playList = new PlayList(timeline[loop]);
        setConstraints(playList, 8, 0, 2, 10);
        //this.addColumn(8,new Node[]{playList});
        getChildren().add(playList);
        playList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    Object selectedItem = playList.getSelectionModel().getSelectedItem();
                    Text item = (Text) selectedItem;
                    String position = item.getText().split("/")[1];
                    timeline[loop].deleteAt(Double.parseDouble(position));
                } catch (NullPointerException exception) {
                } catch (Exception exception) {
                }
                
            }
        });
        playList2 = new PlayListRepeat(timeline);
        setConstraints(playList2, 10, 0, 2, 10);
        getChildren().add(playList2);
        //this.addColumn(10,playList);
        playList2.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    Object selectedItem = playList2.getSelectionModel().getSelectedItem();
                    Text item = (Text) selectedItem;
                    String position = item.getText().split("/")[1];
                    timeline[loop].deleteAt(Double.parseDouble(position));
                } catch (NullPointerException exception) {
                } catch (Exception exception) {
                }
                
            }
        });
        textTimeline = new TextField("time");
        this.getChildren().add(textTimeline);
        setConstraints(textTimeline, 1, 10);
        listFolderFiles = new ListFolderFiles(timeline, loopTimer[loop], this);
        listFolderFiles.setInitialDirectory(new File("./rythmFiles"));
        //fileChooser.showOpenDialog(null);
        listFolderFiles.listFiles();
        setConstraints(listFolderFiles, 6, 0, 2, 10);
        getChildren().add(listFolderFiles);
        //this.addColumn(2,listFolderFiles);
        
        CheckBox loopCheckBox = new CheckBox("Loop?");
        loopCheckBox.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                timeline[loop].setLoops(loopCheckBox.isSelected());
            }
        });
        loopCheckBox.setSelected(true);
        setConstraints(loopCheckBox, 9, 10, 1, 1);
        getChildren().add(loopCheckBox);
        nbrLoops = new TextField("1");
        setConstraints(nbrLoops, 10, 10, 1, 1);
        getChildren().add(nbrLoops);
        
        TextField loopText = new TextField("Loaded loop°" + loop);
        this.getChildren().add(loopText);
        setConstraints(loopText, 0, 9);
        
        Button saveButton = new Button("save");
        this.getChildren().add(saveButton);
        setConstraints(loopText, 1, 9);
        Button loadButton = new Button("load");
        this.getChildren().add(loadButton);
        setConstraints(loopText, 1, 9);
        this.loop = loop;
        loadLoop(loop);

        timelineThread= new TimelineThread(this);
        timelineThread.start();

        Service<Void> fileSaveService = new Service<Void>(){
            @Override
            protected Task<Void> createTask() {
                return new MyDrawingTask();
            }

            class MyDrawingTask extends Task{

                @Override
                protected Object call() throws Exception {
                    while(true) {
                        Thread.sleep(2000);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                writeNoLoop(loopTimer[loop].getLoop());
                                writeTimeLoop(loop);
                                playList.display();

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                XmlTimeline xmlTimeline = new XmlTimeline(timeline);
                                xmlTimeline.save();
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                }
            }

        };
        fileSaveService.start();
    }


    private void loadLoop(int loop) {
        if (loop >= 0 && loop < size) {
            setLoop(loop);
            this.loop = loop;
            TextField loopText = new TextField("Loaded loop°" + loop);
            this.getChildren().add(loopText);
            setConstraints(loopText, 0, 9);
            tempoText.setText("" + tempo[loop]);        }
    }
    
    private void setLoop(int loop) {
        playList.setTimeline(timeline[loop]);
        playList2.setTimeline(timeline);
        listFolderFiles.setLoop(loop);
        listFolderFiles.setTimeline(timeline[loop]);
        loopTimer[loop] = new LoopTimer(this, loop);
        playList2.setLoop(loop);
        playList2.display();
        playList.display();
        listFolderFiles.listFiles();
    }

    public void writeNoLoop(int nbrLoops) {
        loops[loop] = nbrLoops;
        this.nbrLoops.setText(""+nbrLoops);
    }

    public double getTimeNowLoopSec(int track) {
        return loopTimer[track].getCurrentTimeOnLineSec();
    }

    public void writeTimeLoop(int track) {
        double newTime = getLoopTimer()[track].getCurrentTimeOnLineSec();
        textTimeline.setText(String.valueOf(newTime));

    }

    public LoopTimer[] getLoopTimer() {
        return loopTimer;
    }

}
