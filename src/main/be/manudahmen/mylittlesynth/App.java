package be.manudahmen.mylittlesynth;

import be.manudahmen.mylittlesynth.processor.WaveForm;
import be.manudahmen.mylittlesynth.rythms.RythmPanel;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.RotateEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class App extends Application {
   private Slider slider;
   private SoundProductionSystem soundProductionSystem;
   private ToggleGroup group;
   private Player player;
   private AudioViewer audioViewer;
   Map noteMap;
   private double minDurationSec = 4.0D;
   private Slider volume;
   private Button[] buttons;
   private int[] buttonNo;

   public SoundProductionSystem getSoundProductionSystem() {
      return this.soundProductionSystem;
   }

   public void setSoundProductionSystem(SoundProductionSystem soundProductionSystem) {
      this.soundProductionSystem = soundProductionSystem;
   }

   public static void main(String[] args) {
      launch(args);
   }

   public void start(Stage primaryStage) {
      this.setSoundProductionSystem(new SoundProductionSystem(60.0F));
      Parent root = new AnchorPane();
      root.minWidth(800.0D);
      root.minHeight(600.0D);
      Scene scene = new Scene(root);
      this.buttons = new Button[16];
      this.buttonNo = new int[16];
      HBox[] pane = new HBox[]{new HBox(), new HBox()};
      BorderPane bp = new BorderPane();
      BorderPane bp2 = new BorderPane();
      bp.setTop(pane[0]);
      bp.setCenter(pane[1]);
      String[] notes = new String[]{"do", "re", "mi", "fa", "sol", "la", "si", "do", "do#", "re#", "--", "fa#", "sol#", "la#", "--", "do#"};
      Canvas canvas = new Canvas(640.0D, 480.0D);
      canvas.setLayoutX(640.0D);
      canvas.setLayoutY(480.0D);
      Pane pane1 = new Pane();
      pane1.getChildren().add(canvas);
      this.slider = new Slider();
      this.slider.setMin(0.0D);
      this.slider.setMax(12.0D);
      this.slider.setValue(4.0D);
      this.slider.setMinorTickCount(1);
      this.slider.setAccessibleText("Octaves");
      this.slider.setOnMouseReleased((event) -> {
         int value = (int)((Slider)event.getSource()).getValue();
         this.player.setOctave(value);
         System.out.println("Octave: " + value);
      });
      this.noteMap = new HashMap();

      int i;
      for(i = 0; i < this.buttons.length; ++i) {
         this.buttons[i] = new Button(notes[i]);
         this.buttons[i].setLayoutX((double)(100 + i * 90));
         this.buttons[i].setLayoutY((double)(150 + (i >= 8 ? 60 : 0)));
         this.buttons[i].setId("Button" + i);
         this.buttons[i].arm();
         pane[i < 8 ? 0 : 1].getChildren().add(this.buttons[i]);
      }

      for(i = 0; i < 96; ++i) {
         this.noteMap.put(i, new Note(this.noteMap, this.minDurationSec, i, WaveForm.SIN, new Enveloppe(this.minDurationSec)));
      }

      KeyCode[] keycode = new KeyCode[]{KeyCode.A, KeyCode.Z, KeyCode.E, KeyCode.R, KeyCode.T, KeyCode.Y, KeyCode.U, KeyCode.I, KeyCode.Q, KeyCode.S, KeyCode.D, KeyCode.F, KeyCode.G, KeyCode.H, KeyCode.J, KeyCode.K};

      for(i = 0; i < this.buttons.length; ++i) {
         this.buttons[i].setOnMousePressed((event) -> {
            this.playNote((Button)event.getSource());
         });
         this.buttons[i].setOnMouseReleased((event) -> {
            this.stopNote((Button)event.getSource());
         });
         this.buttons[i].setOnTouchPressed((event) -> {
            this.playNote((Button)event.getSource());
         });
         this.buttons[i].setOnTouchReleased((event) -> {
            this.stopNote((Button)event.getSource());
         });
         this.buttons[i].setPadding(new Insets(10.0D, 10.0D, 10.0D, 10.0D));
         this.buttonNo[i] = this.getTone(this.buttons[i]);
      }

      VBox vBox = new VBox();
      this.group = new ToggleGroup();
      RadioButton radioButton = new RadioButton();
      radioButton.setText("Sine");
      radioButton.setToggleGroup(this.group);
      radioButton.setUserData(WaveForm.SIN);
      radioButton.setSelected(true);
      radioButton.setOnAction((event) -> {
         RadioButton source = (RadioButton)event.getSource();
         String value = source.getText();
         this.setForm((WaveForm)source.getUserData());
      });
      vBox.getChildren().add(radioButton);
      radioButton = new RadioButton();
      radioButton.setText("Square");
      radioButton.setUserData(WaveForm.RECT);
      radioButton.setToggleGroup(this.group);
      radioButton.setOnAction((event) -> {
         RadioButton source = (RadioButton)event.getSource();
         String value = source.getText();
         this.setForm((WaveForm)source.getUserData());
      });
      vBox.getChildren().add(radioButton);
      radioButton = new RadioButton();
      radioButton.setText("Triangle");
      radioButton.setToggleGroup(this.group);
      radioButton.setUserData(WaveForm.TRI);
      radioButton.setOnAction((event) -> {
         RadioButton source = (RadioButton)event.getSource();
         String value = source.getText();
         this.setForm((WaveForm)source.getUserData());
      });
      vBox.getChildren().add(radioButton);
      radioButton = new RadioButton();
      radioButton.setText("Sawtooth");
      radioButton.setUserData(WaveForm.SAWTOOTH);
      radioButton.setToggleGroup(this.group);
      radioButton.setOnAction((event) -> {
         RadioButton source = (RadioButton)event.getSource();
         String value = source.getText();
         this.setForm((WaveForm)source.getUserData());
      });
      vBox.getChildren().add(radioButton);
      boolean recording = false;
      Button record = new Button("REC SAMPLE");
      vBox.getChildren().add(record);
      record.setOnAction((actionEvent) -> {
         this.player.toggleRecording();
      });
      Button stop = new Button("STOP REPEAT");
      vBox.getChildren().add(stop);
      stop.setOnAction((actionEvent) -> {
         this.player.setRecording(false);
         this.player.setPlayingBuffer(false);
      });
      Button wav = new Button("WAVE");
      vBox.getChildren().add(wav);
      wav.setOnAction((actionEvent) -> {
         this.setSoundProductionSystem(new SoundProductionSystem());
      });
      Button roundButton = new Button();
      roundButton.setOnRotate(rotateEvent -> System.out.println("rotate" + rotateEvent.getAngle()));
      roundButton.setOnRotationFinished(rotateEvent -> System.out.println("rotation finish" + rotateEvent.getAngle()));
      vBox.getChildren().add(roundButton);
      this.audioViewer = new AudioViewer(44100.0F, 2, canvas);
      Slider zoomSlider = new Slider();
      zoomSlider.setMin(1.0D);
      zoomSlider.setMax(20.0D);
      zoomSlider.setValue(1.0D);
      this.audioViewer.setZoom(1.0D);
      zoomSlider.setOnMouseReleased((mouseEvent) -> {
         this.audioViewer.setZoom(zoomSlider.getValue());
      });
      vBox.getChildren().add(zoomSlider);
      bp.setLeft(vBox);
      bp.setRight(this.slider);
      bp.setBottom(pane1);
      bp2.setTop(bp);
      bp2.setCenter(canvas);
      TitledPane[] titledPanes = new TitledPane[]{new TitledPane(), null};
      titledPanes[0].setOnKeyPressed((event) -> {
         for(int i1 = 0; i1 < this.buttons.length; ++i1) {
            if (event.getCode().equals(keycode[i1])) {
               this.playNote(this.buttons[i1]);
               System.out.println("playNote" + this.player.getCurrentNotes().size());
            }
         }

      });
      titledPanes[0].setOnKeyReleased((event) -> {
         for(int i2 = 0; i2 < this.buttons.length; ++i2) {
            if (event.getCode().equals(keycode[i2])) {
               this.stopNote(this.buttons[i2]);
               System.out.println("stopNote" + this.player.getCurrentNotes().size());
            }
         }

      });
      titledPanes[0].setContent(bp2);
      bp2.setPadding(new Insets(20.0D, 20.0D, 20.0D, 20.0D));
      titledPanes[1] = new TitledPane();
      vBox = new VBox();
      RythmPanel rythmPanel = new RythmPanel(titledPanes[1], this);
      vBox.getChildren().addAll(new Node[]{rythmPanel});
      vBox.getChildren().add(rythmPanel.getGridPaneTime());
      titledPanes[1].setContent(vBox);
      Accordion accordion = new Accordion();
      accordion.getPanes().addAll(titledPanes);
      scene.setRoot(accordion);
      accordion.setExpandedPane(titledPanes[0]);
      bp2.setBottom(this.volume = new Slider());
      this.volume.setMin(0.0D);
      this.volume.setMax(100.0D);
      this.volume.setValue(100.0D);
      this.volume.setAccessibleText("Volume:" + this.volume.getValue());
      this.volume.setOnMouseReleased((event) -> {
         this.player.setVolume(this.volume.getValue());
         System.out.println("Volume du player: " + this.player.getVolume());
      });
      primaryStage.setTitle("MyLittleSynth 5.0");
      primaryStage.setScene(scene);
      primaryStage.show();
      primaryStage.setOnCloseRequest(event -> {
         App.this.player.setPlaying(false);
         App.this.audioViewer.setRunning(false);
         App.this.getSoundProductionSystem().write();
         App.this.getSoundProductionSystem().end();
         App.this.getSoundProductionSystem().setEnded(true);
         System.exit(0);
      });
      this.player = new Player(this, this.audioViewer);
      this.player.setRecording(false);
      this.player.setVolume(100.0D);
      this.player.start();
   }

   public int getTone(Object source) {
      String id = ((Button)source).getText();
      int index = 0;
      String note = "";
      String diese = id.substring(0, 2);
      byte var6 = -1;
      switch(diese.hashCode()) {
      case 3211:
         if (diese.equals("do")) {
            var6 = 0;
         }
         break;
      case 3259:
         if (diese.equals("fa")) {
            var6 = 3;
         }
         break;
      case 3445:
         if (diese.equals("la")) {
            var6 = 5;
         }
         break;
      case 3484:
         if (diese.equals("mi")) {
            var6 = 2;
         }
         break;
      case 3635:
         if (diese.equals("re")) {
            var6 = 1;
         }
         break;
      case 3670:
         if (diese.equals("si")) {
            var6 = 6;
         }
         break;
      case 3676:
         if (diese.equals("so")) {
            var6 = 4;
         }
      }

      index = 0;
      switch(var6) {
      case 0:
         note = "C";
         index = 2;
         break;
      case 1:
         note = "D";
         index = 2;
         break;
      case 2:
         note = "E";
         index = 2;
         break;
      case 3:
         note = "F";
         index = 2;
         break;
      case 4:
         note = "G";
         index = 3;
         break;
      case 5:
         note = "A";
         index = 2;
         break;
      case 6:
         note = "B";
         index = 2;
         break;
      default:
         return 0;
      }

      diese = "";
      if (index < id.length()) {
         switch(id.charAt(index)) {
         case '#':
            diese = "#";
            break;
         default:
            diese = "";
         }
      }

      int octave = (int)this.slider.getValue();
      String noteAnglaise = note + octave + diese;
      System.out.println(noteAnglaise);
      return this.soundProductionSystem.equiv(noteAnglaise);
   }

   public void setForm(WaveForm userData) {
      if (userData != null) {
         this.player.setWaveform(userData);
      } else {
         System.out.println("null waveform");
      }

   }

   public void playNote(Button source) {
      System.out.println("Tone:  " + this.getTone(source) + "  Note :" + source.getText() + "  Octave: " + (int)this.slider.getValue() + "\nVolume: " + this.player.getVolume());
      Note note = (Note)this.noteMap.get(this.getTone(source));
      try {
         note.setEnveloppe(new Enveloppe(this.minDurationSec));
      }
      catch (NullPointerException ex)
      {
         System.out.println("Error playNote" + note.getTone());
      }note.setWaveform(this.player.getForm());
      this.player.playNote(note);
   }

   public void stopNote(Button source) {
      Note note = (Note)this.noteMap.get(this.getTone(source));

      assert note != null;

      System.out.println((double)note.getTimer().getTotalTimeElapsed() / 1.0E9D);
      this.player.stopNote(note);
      System.out.println("Key pressed: " + this.player.getCurrentNotes().size());
   }

   public Button getButton(Integer halfTone) {
      for(int i = 0; i < this.buttons.length; ++i) {
         if ((new Integer(halfTone)).equals(this.buttonNo[i])) {
            return this.buttons[i];
         }
      }

      return null;
   }

   public Player getPlayer() {
      return this.player;
   }
}
