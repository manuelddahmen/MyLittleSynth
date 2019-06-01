package one.empty3.apps.mylittlesynth.rythms;


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

public class RythmPanel extends GridPane {
    private final App app;
    TextField textTimeline;
    public int loop = 0;
    private int timelineSizeDefault = 16;
    
    double timelineTimeSec() {
        return 60. / tempo[loop];
    }
    
    static int size = 8;
    private double[] tempo = new double[size];
    int columnCount = 4;
    private int[] timelineSize = new int[size];
    int buttonsCount = size * columnCount;
    private TitledPane container;
    private ResourceBundle resourceBundle;
    RythmModel[] model;
    Timeline[] timeline = new Timeline[size];
    private LoopProgress gridPaneTime;
    private Button[] buttons = new Button[size];
    private TimelineThread[] timelineThread = new TimelineThread[size];
    private MyTimer[] myTimer = new MyTimer[size];
    private TextField tempoText;
    LoopTimer[] loopTimer = new LoopTimer[size];
    private GridPane paneLine;
    private ListFolderFiles listFolderFiles;
    PlayList playList;
    PlayListRepeat playList2;
    
    
    void setTempo(double tempoCand) {
        this.tempo[loop] = tempo[loop] > 0 ? tempoCand : 1;
    }
    
    public LoopProgress getCanvasTimelineProgress() {
        return this.gridPaneTime
                ;
    }
    
    public RythmPanel(TitledPane container, App app) {
        this.container = container;
        this.model = new RythmModel[size];
        for (int i = 0; i < size; i++)
            this.model[i] = new RythmModel(this, new File("rythmFiles"));
        this.app = app;
        init();
    }
    
    public void init() {
        // Init components;
        for (int i = 0; i < size; i++)
            tempo[i] = 20;
        loopTimer[loop] = new LoopTimer(this);
        for (int i = 0; i < size; i++)
            timeline[i] = new Timeline(this);
        XmlTimeline xmlTimeline = new XmlTimeline();
        xmlTimeline.setAudioSamples(timeline);
        this.setGridLinesVisible(true);
        
        
        for (int j = 0; j < size / columnCount; j++)
            for (int i = 0; i < columnCount; i++) {
                buttons[j * columnCount + i] = new Button("Loop #" + (j * columnCount + i));
                setConstraints(buttons[j * columnCount + i], i, j);
                buttons[j * columnCount + i].setOnMouseClicked(event -> {
                    String name = ((Button) event.getSource()).getText();
                    int loop2 = Integer.parseInt(name.split("#")[1]);
                    loadLoop(loop2);
                });
            }
        getChildren().addAll(buttons);
        timelineSize[loop] = timelineSizeDefault;
        LoopProgress progress = new LoopProgress(this);
        this.gridPaneTime = progress;
        setConstraints(progress, 0, 0);
        this.getChildren().addAll(progress);
        Slider slider = new Slider(0.1D, 20.0D, 1.0D);
        setConstraints(slider, 6, 0);
        this.getChildren().addAll(new Node[]{slider});
        slider.setValueChanging(true);
        myTimer[loop] = new RythmPanel.MyTimer();
        
        tempoText = new TextField("" + tempo[loop]);
        tempoText.setOnAction(event -> {
            try {
                double tempoValueCandidate = Double.parseDouble(tempoText.getText());
                tempo[loop] = tempoValueCandidate > 0 ? tempoValueCandidate : 1;
                System.out.println("Tempo: " + tempo[loop]);
            } catch (Exception ex) {
            
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
        
        
        for (int i = 0; i < size; i++) {
            timelineThread[i] = new TimelineThread(this.timeline);
            timelineThread[i].start();
            timelineThread[i].setLoop(i);
        }
        
        
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
        
        CheckBox desising = new CheckBox("Decreasing");
        desising.setSelected(false);
        desising.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                timeline[loop].setDecresing(desising.isSelected());
            }
        });
        setConstraints(desising, 9, 10, 1, 1);
        getChildren().add(desising);
        TextField nbrLoops = new TextField("1");
        nbrLoops.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                timeline[loop].setNbrLoops(Integer.parseInt(nbrLoops.getText()));
            }
        });
        setConstraints(nbrLoops, 10, 10, 1, 1);
        getChildren().add(nbrLoops);
        
        TextField loopText = new TextField("Loaded loop #" + loop);
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
    }
    
    private void loadLoop(int loop) {
        if (loop >= 0 && loop < size) {
            setLoop(loop);
            this.loop = loop;
            
            TextField loopText = new TextField("Loaded loop #" + loop);
            this.getChildren().add(loopText);
            setConstraints(loopText, 0, 9);
        }
    }
    
    private void setLoop(int loop) {
        playList.setTimeline(timeline[loop]);
        playList2.setTimeline(timeline);
        listFolderFiles.setLoop(loop);
        timelineThread[loop].setLoop(loop);
        listFolderFiles.setTimeline(timeline[loop]);
        loopTimer[loop] = new LoopTimer(this);
        myTimer[loop] = new MyTimer();
        playList2.setLoop(loop);
        playList2.display();
        playList.display();
        listFolderFiles.listFiles();
    }
    
    class MyTimer extends AnimationTimer {
        private Image state = null;
        private Image imageDecline = null;
        
        MyTimer() {
            super();
        }
        
        public void handle(long now) {
            int i = (int) (loopTimer[loop].
                    getCurrentTimeOnLineSec() /
                    timeline[loop]
                            .getDuration()
                    * timelineSize
                    [loop]);
            if (imageDecline == null)
                try {
                    imageDecline = new Image(new FileInputStream(new File("resources/decline-button.png")));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            if (state == null)
                try {
                    state = new Image(new FileInputStream(new File("resources/button-states.png")));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            
            
        }
    }
}
