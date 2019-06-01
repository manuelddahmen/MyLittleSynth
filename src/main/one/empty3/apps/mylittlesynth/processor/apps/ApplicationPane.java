package one.empty3.apps.mylittlesynth.processor.apps;

import one.empty3.apps.mylittlesynth.processor.AudioViewer;
import one.empty3.apps.mylittlesynth.processor.Oscillator;
import one.empty3.apps.mylittlesynth.processor.OutputProcessToLine;
import one.empty3.apps.mylittlesynth.processor.WaveForm;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ApplicationPane extends Application {
   private final ApplicationPane app = this;
   private Canvas drawingZone1;

   public void start(Stage primaryStage) throws Exception {
      this.drawingZone1 = new Canvas(1200.0D, 100.0D);
      this.drawingZone1.setWidth(400.0D);
      this.drawingZone1.setHeight(200.0D);
      this.drawingZone1.autosize();
      GraphicsContext gc = this.drawingZone1.getGraphicsContext2D();
      gc.strokeText("audio viewer", 150.0D, 100.0D);
      Pane root = new Pane();
      root.setStyle("-fx-padding: 10;-fx-border-style: solid inside;-fx-border-width: 2;-fx-border-insets: 5;-fx-border-radius: 5;-fx-border-color: blue;");
      root.autosize();
      root.getChildren().add(this.drawingZone1);
      Scene scene = new Scene(root);
      primaryStage.setScene(scene);
      primaryStage.setTitle("Audio Viewer");
      primaryStage.show();
      primaryStage.setOnCloseRequest((event) -> {
         System.exit(0);
      });
      (new ApplicationPane.DrawingTask()).start();
   }

   public Canvas getCanvas() {
      return this.drawingZone1;
   }

   public Canvas getGraphicsContext() {
      return this.getDrawingZone1();
   }

   public static void main(String... args) {
      launch(new String[0]);
   }

   public Canvas getDrawingZone1() {
      return this.drawingZone1;
   }

   class DrawingTask extends Thread {
      public void run() {
         Oscillator os1 = new Oscillator(440.0D, 1.0D, WaveForm.SIN);
         AudioViewer audioViewer = new AudioViewer(44100.0F, 1, ApplicationPane.this.app);
         OutputProcessToLine outputProcessToLine = new OutputProcessToLine(80.0D);
         os1.getOutProcessors().put("Drawing samples", audioViewer);
         os1.getOutProcessors().put("Playing 16bits Mono", outputProcessToLine);
         audioViewer.start();
         System.out.println("Oscillitor outs: " + os1.getOutProcessors().size());
         os1.start();
         outputProcessToLine.start();
      }
   }
}
