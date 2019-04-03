package be.manudahmen.mylittlesynth.rythms;

import be.manudahmen.mylittlesynth.App;
import be.manudahmen.mylittlesynth.Timer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class RythmPanel extends GridPane {
   private double tempo = 20;
   protected double timelineTimeSec(){return  60./tempo; }
   private int size = 8;
   private int columnCount = 4;
   private int timelineSize = 4;
   private int buttonsCount = size*columnCount;
   private TitledPane container;
   private ResourceBundle resourceBundle;
   private RythmPanel.Model model;
   private ListView listView;
   private Timeline timeline = new Timeline(this);
   private GridPane gridPaneTime;
   private Button[] buttonLine;
   private TextField tempoText;
   RythmPanel.LoopTimer loopTimer = new RythmPanel.LoopTimer();

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

      ObservableList files = FXCollections.observableArrayList();
      this.listView = new ListView();
      Button buttonLoad = new Button("Load...");
      setConstraints(buttonLoad, 5, 0);
      this.getChildren().addAll(new Node[]{buttonLoad});
      buttonLoad.setOnMouseClicked((mouseEvent) -> {
         FileChooser fileChooser = new FileChooser();
         fileChooser.setTitle("Choose files");
         List files2 = fileChooser.showOpenMultipleDialog((Window)null);
         if (files2 == null || files2.size() > 0) {
            this.model.init(files2);
            FileCell[] list2 = new FileCell[files2.size()];

            for(int d = 0; d < files2.size(); ++d) {
               list2[d] = new FileCell((File)files2.get(d));
            }

            this.listView.getItems().addAll(files2);
         }

      });
      this.listView.getItems().addAll(this.model.files);
      setConstraints(this.listView, 5, 1, 5, 10);
      this.getChildren().addAll(new Node[]{this.listView});
      this.listView.setOnDragEntered(dragEvent -> System.out.println("Drag entered: "));
      this.listView.setOnDragDone(dragEvent -> {
         Object source = dragEvent.getSource();
         EventTarget target = dragEvent.getTarget();
         System.out.println("Drag item : ");
      });
      Button clearList = new Button("Clear list");
      setConstraints(clearList, 6, 0);
      this.getChildren().addAll(new Node[]{clearList});
      clearList.setOnMouseClicked((mouseEvent) -> {
         this.listView.getItems().clear();
      });
      this.gridPaneTime = new GridPane();
      this.buttonLine = new Button[timelineSize];

      for(int i = 0; i < timelineSize; ++i) {
         this.buttonLine[i] = new Button("" + i);
         GridPane var10000 = this.gridPaneTime;
         GridPane.setConstraints(this.buttonLine[i], i, 0);
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
      myTimer.start();
      (new ThreadPlaying(this.loopTimer, this.timeline)).start();
      tempoText = new TextField("100");
      tempoText.setOnAction(new EventHandler<ActionEvent>(){
         @Override
         public void handle(ActionEvent event) {
            try {
               tempo = Integer.parseInt(tempoText.getText());
               System.out.println("Tempo: " + tempo);
            }
            catch (Exception ex)
            {

            }
         }

      });
      tempoText.onScrollProperty().set(new EventHandler<ScrollEvent>() {
         @Override
         public void handle(ScrollEvent event) {
            tempo = (int)(event.getTouchCount()+tempo);
            tempoText.setText(""+tempo);
         }
      });
      this.getChildren().add(tempoText);
   }

   class MyTimer extends AnimationTimer {
      public void handle(long now) {
         int i = (int)timeline.getDurationPC();
         Image imageDecline = null;
         try {
            imageDecline = new Image(new FileInputStream(new File("resources/decline-button.png")));
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         }
         Image state = null;
         try {
            state = new Image(new FileInputStream(new File("resources/button-states.png")));
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         }
         if (i >= 0 && i < timelineSize) {
            RythmPanel.this.buttonLine[i].setGraphic(new ImageView(state));
         }

         for(int j = 0; j < timelineSize && i != j; ++j) {
            RythmPanel.this.buttonLine[j].setGraphic(new ImageView(imageDecline));
         }

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
         ListView source = RythmPanel.this.listView;

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
                  try {
                     double d = loopTimer.getCurrentTimeOnLineSec();
                     timeline.addFileAtTimePC(d/timelineTimeSec(), file);
                     System.out.println(d*timelineTimeSec());
                     AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
                     (new Thread(new PlayWave(audioInputStream))).start();
                  } catch (IOException var6) {
                     var6.printStackTrace();
                  } catch (UnsupportedAudioFileException var7) {
                     var7.printStackTrace();
                  }

               });
            }

            Button var10000 = this.buttons[i];
         }

      }
   }

   class LoopTimer extends Timer {
      public LoopTimer()
      {
         super();
         init();
      }

       public double getCurrentTimeOnLineSec() {
          double remainder = Math.IEEEremainder((getTotalTimeElapsedNanoSec()+timelineTimeSec()*1E9d),
                  timelineTimeSec()/1E9d)*1E9;
          return remainder;
       }
   }
}
