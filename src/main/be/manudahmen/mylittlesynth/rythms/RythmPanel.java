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
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class RythmPanel extends GridPane {
   private final App app;
   private double tempo = 60;
   private TimelineThread timelineThread;
   TextField textTimeline;
   private GridPane paneLine;
   private ListFolderFiles listFolderFiles;

   double timelineTimeSec(){return  60./tempo; }
   private int size = 8;
   private int columnCount = 4;
   private int timelineSize =  16;
   private int buttonsCount = size*columnCount;
   private TitledPane container;
   private ResourceBundle resourceBundle;
   private RythmPanel.Model model;
   private Timeline timeline = new Timeline(this);
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
      this.model = new RythmPanel.Model();
      this.model.modelFromDirectory("rythmFiles");
      this.app = app;
      this.init();
   }

   public void init() {

      this.setGridLinesVisible(true);




      for(int i = 0; i < size; ++i) {
         for(int j = 0; j < columnCount; ++j) {
            Node[] nodes = new Node[columnCount];
            nodes[j] = this.model.buttons[i * columnCount + j];
            setConstraints(nodes[j], j, i);
            this.getChildren().addAll(new Node[]{nodes[j]});
         }
      }

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

      textTimeline = new TextField("Time0");
      this.getChildren().add(textTimeline);
      setConstraints(textTimeline, 1, 10);
      listFolderFiles = new ListFolderFiles(timelineThread, loopTimer);
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
         RythmPanel.this.buttonLine[i].setGraphic(new ImageView(state));


      }
   }

   public class Model {
      private List files = new ArrayList(buttonsCount);
      private Button[] buttons = new Button[buttonsCount];

      public void modelFromDirectory(String directory) {
         String[] fileArray = (new File(directory)).list();
         int var3 = fileArray.length;

         int var4;
         for(var4 = 0; var4 < var3; ++var4) {
            String s = fileArray[var4];
            this.files.add(new File(directory + "/" + s));
         }

         int i = 0;
         Button[] var9 = this.buttons;
         var4 = var9.length;

         for(int var10 = 0; var10 < var4; ++var10) {
             if(this.files.size()>i) {
                 Button var10000 = var9[var10];
                 String filename = ((File) this.files.get(i)).toString();
                 this.buttons[i] = new Button(filename);
                 ++i;
             }
         }

         this.init(this.files);
      }

      public synchronized void init(List files) {
         ListView source = RythmPanel.this.listFolderFiles;

         for(int i = 0; i < Math.min(files.size(), buttonsCount); ++i) {
            File file = (File)files.get(i);
            if (!file.exists()) {
               System.out.println("error file not exist");
               System.exit(-1);
            }

            if (file.getName().endsWith(".wav")) {
               String name = file.getName().toString();
               if (this.buttons[i] == null) {
                  this.buttons[i] = new Button(name);
               } else {
                  this.buttons[i].setText(name);
               }

               this.buttons[i].setOnMouseClicked((mouseEvent) -> {
                     double d = loopTimer.getCurrentTimeOnLineSec();
                     timeline.addFileAtTimePC(d/timelineTimeSec(), file);
                     System.out.println(d);

               });
            }

            Button var10000 = this.buttons[i];
         }

      }
   }
/*
   class CanvasPaint extends AnimationTimer
   {
      private Canvas canvas;
      public CanvasPaint(Canvas canvas)
      {
         CanvasPaint.this.canvas  = canvas;
      }
      @Override
      public void handle(long now) {
         double duration = timeline.getDuration();
         GraphicsContext graphicsContext2D = canvas.getGraphicsContext2D();
         graphicsContext2D.setFill(Color.BLACK);
         graphicsContext2D.fill();

      }
   }
 */

}
