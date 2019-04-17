package be.manudahmen.mylittlesynth.rythms;

import be.manudahmen.mylittlesynth.App;
import be.manudahmen.mylittlesynth.Timer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.jmx.MXNodeAlgorithm;
import com.sun.javafx.jmx.MXNodeAlgorithmContext;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultElement;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class RythmPanel extends GridPane {
   private final App app;
   private double tempo = 60;
   private TimelineThread timelineThread;
   TextField textTimeline;
   private GridPane paneLine;
   private ListFolderFiles listFolderFiles;
   PlayList playList;
   double timelineTimeSec(){return  60./tempo; }
   int size = 8;
   int columnCount = 4;
   private int timelineSize =  16;
   int buttonsCount = size*columnCount;
   private TitledPane container;
   private ResourceBundle resourceBundle;
   RythmModel model;
   Timeline timeline = new Timeline(this);
   private GridPane gridPaneTime;
   private Button[] buttonLine;
   private TextField tempoText;
   LoopTimer loopTimer = new LoopTimer(this);

   public TimelineThread getTimelineThread() {
      return timelineThread;
   }

   public void setTimelineThread(TimelineThread timelineThread) {
      this.timelineThread = timelineThread;
   }

   public GridPane getGridPaneTime() {
      return this.gridPaneTime;
   }

   public void setGridPaneTime(GridPane gridPaneTime) {
      this.gridPaneTime = gridPaneTime;
   }

   public RythmPanel(TitledPane container, App app) {
      this.container = container;
      this.model = new RythmModel(this, new File("rythmFiles"));
      this.app = app;
      this.init();
   }

   public void init() {

      this.setGridLinesVisible(true);






      this.gridPaneTime = new GridPane();
      this.buttonLine = new Button[timelineSize];
      for(int i = 0; i < timelineSize; ++i) {

         this.buttonLine[i] = new Button("" + i+1);
         GridPane var10000 = this.gridPaneTime;
          setConstraints(this.buttonLine[i], i, 0);
         this.gridPaneTime.getChildren().addAll(new Node[]{this.buttonLine[i]});
         this.gridPaneTime.setOnMouseClicked((mouseEvent) -> {
         });
      }
      setConstraints(this.gridPaneTime, 5, 0);
      this.getChildren().addAll(new Node[]{this.gridPaneTime});
      Slider slider = new Slider(0.1D, 20.0D, 1.0D);
      setConstraints(slider, 6, 0);
      this.getChildren().addAll(new Node[]{slider});
      slider.setValueChanging(true);
      RythmPanel.MyTimer myTimer = new RythmPanel.MyTimer();
      tempoText = new TextField("100");
      tempoText.setOnAction(new EventHandler<ActionEvent>() {
                               @Override
                               public void handle(ActionEvent event) {
                                  try {
                                     tempo = Integer.parseInt(tempoText.getText());
                                     System.out.println("Tempo: " + tempo);
                                  } catch (Exception ex) {

                                  }
                               }
                            });
      tempoText.onScrollProperty().set(new EventHandler<ScrollEvent>() {
        @Override
         public void handle(ScrollEvent event) {
           int sign = 0;
           if (event.getX() > 0) {
              sign = 1;
           } else if (event.getX() <= 0) {
              sign = -1;
           }
            tempo = (int)(tempo+sign);
            tempoText.setText(""+tempo);
         }
      });
      this.getChildren().add(tempoText);
      setConstraints(tempoText, 0, 10);

      timelineThread = new TimelineThread(this.timeline);
      timelineThread.start();

      playList = new PlayList(timeline);
      setConstraints(playList, 8,  0, 2, 10);
      getChildren().add(playList);
      playList.setOnMouseClicked(new EventHandler<MouseEvent>() {
         @Override
         public void handle(MouseEvent event) {
            Object selectedItem = playList.getSelectionModel().getSelectedItem();
            Text item = (Text) selectedItem;
            String position = item.getText().split("/")[1];
            try {
                  timeline.deleteAt(Double.parseDouble(position));
            }catch(Exception ex)
               {}

         }
      });
      textTimeline = new TextField("Time0");
      this.getChildren().add(textTimeline);
      setConstraints(textTimeline, 1, 10);
      listFolderFiles = new ListFolderFiles(timelineThread, loopTimer, this);
      listFolderFiles.setInitialDirectory(new File("./rythmFiles"));
      //fileChooser.showOpenDialog(null);
      listFolderFiles.listFiles();
      setConstraints(listFolderFiles, 6, 0, 2, 10);
      getChildren().add(listFolderFiles);

      myTimer.start();
   }
   class MyTimer extends AnimationTimer {
      private Image state = null;
      private Image imageDecline = null;

      MyTimer()
      {
         super();
      }
      public void handle(long now) {
         int i = (int)(loopTimer.getCurrentTimeOnLineSec()/timeline.getDuration()*timelineSize);
         if(imageDecline==null)
         try {
            imageDecline = new Image(new FileInputStream(new File("resources/decline-button.png")));
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         }
         if(state==null)
         try {
            state  = new Image(new FileInputStream(new File("resources/button-states.png")));
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         }
         for(int j = 0; j < timelineSize && i != j; ++j) {
            RythmPanel.this.buttonLine[j].setGraphic(new ImageView(imageDecline));
         }
         RythmPanel.this.buttonLine[i<0?0:(i>=timelineSize?timelineSize-1:i)].setGraphic(new ImageView(state));


      }
   }
}
