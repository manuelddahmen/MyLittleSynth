/*
 * This file is part of Plants-Growth-2
 *     Plants-Growth-2 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Plants-Growth-2 is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Plants-Growth-2.  If not, see <http://www.gnu.org/licenses/>.
 */
package be.manudahmen.mylittlesynth;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class App extends Application {
    private Slider slider;
    private SoundProductionSystem soundProductionSystem;
    private ToggleGroup group;
    private Player player;
    private AudioViewer audioViewer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        soundProductionSystem = new SoundProductionSystem();
        Parent root = new AnchorPane();

        root.minWidth(800);
        root.minHeight(600);

        Scene scene = new Scene(root);

        Button[] buttons = new Button[16];

        HBox[] pane = new HBox[2];
        pane[0] = new HBox();
        pane[1] = new HBox();

        BorderPane bl = new BorderPane();

        bl.setTop(pane[0]);
        bl.setCenter(pane[1]);

        String[] notes = new String[]{
                "do", "re", "mi", "fa", "sol", "la", "si", "do",
                "do#", "re#", "--", "fa#", "sol#", "la#", "--", "do#"};


        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new Button(notes[i]);
            buttons[i].setLayoutX(100 + i * 30);
            buttons[i].setLayoutY(150 + (i >= 8 ? 30 : 0));
            buttons[i].setId("Button" + i);
            buttons[i].setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    playNote(event.getSource());
                }
            });
            buttons[i].setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    stopNote(event.getSource());
                }
            });

            pane[i < 8 ? 0 : 1].getChildren().add(buttons[i]);
        }


        VBox vBox = new VBox();

        group = new ToggleGroup();

        RadioButton radioButton = new RadioButton();
        radioButton.setText("Sine");
        radioButton.setToggleGroup(group);
        radioButton.setSelected(true);
        radioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String value = ((RadioButton) event.getSource()).getText();
                player.setForm(value);
            }
        });
        vBox.getChildren().add(radioButton);
        radioButton = new RadioButton();
        radioButton.setText("Square");
        radioButton.setToggleGroup(group);
        vBox.getChildren().add(radioButton);
        radioButton = new RadioButton();
        radioButton.setText("Triangle");
        radioButton.setToggleGroup(group);
        vBox.getChildren().add(radioButton);
        radioButton = new RadioButton();
        radioButton.setText("Sawtooth");
        radioButton.setToggleGroup(group);
        vBox.getChildren().add(radioButton);


        bl.setLeft(vBox);


        slider = new Slider();
        slider.setMin(1);
        slider.setMin(10);
        slider.setValue(4);
        slider.setMinorTickCount(1);
        slider.setAccessibleText("Octaves");
        slider.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                double value = ((Slider) event.getSource()).getValue();
                player.setOctave((int) value);
            }
        });
        bl.setRight(slider);

        Pane pane1 = new Pane();

        Canvas canvas = new Canvas();

        canvas.setWidth(640);
        canvas.setHeight(480);

        pane1.getChildren().add(canvas);

        audioViewer = new AudioViewer(44100, 2, canvas);

        player = new Player(audioViewer);

        bl.setBottom(pane1);


        scene.setRoot(bl);

        primaryStage.setTitle("Plants 2.0 synth");
        primaryStage.setScene(scene);
        //primaryStage.setFullScreen(true);
        //primaryStage.setMaximized(true);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                player.setPlaying(false);
                audioViewer.setRunning(false);
                System.exit(0);
            }
        });

        audioViewer.start();
        player.start();
    }

    public int getTone(Object source) {
        String id = ((Button) source).getText();
        String note;
        int index = 0;
        note = "";
        switch (id.substring(0, 2)) {
            case "do"://DO
                note = "C";
                index = 2;
                break;
            case "re"://RE
                note = "D";
                index = 2;
                break;
            case "mi"://MI
                note = "E";
                index = 2;
                break;
            case "fa"://FA
                note = "F";
                index = 2;
                break;
            case "so"://SOL
                note = "G";
                index = 3;
                break;
            case "la"://LA
                note = "A";
                index = 2;
                break;
            case "si"://SI
                note = "B";
                index = 2;
                break;
            default:
                return 0;
        }
        String diese = "";
        if (index < id.length()) {
            switch (id.charAt(index)) {
                case '#':
                    diese = "#";
                    break;
                default:
                    diese = "";
            }
        }

        int octave = player.getOctave();
        String noteAnglaise = note + octave + diese;
        System.out.println(noteAnglaise);
        return soundProductionSystem.equiv(noteAnglaise);

    }

    public void setForm() {
        SoundProductionSystem.Waveform waveform = SoundProductionSystem.Waveform.SIN;
        RadioButton selectedRadioButton = (RadioButton) group.getSelectedToggle();
        String toogleGroupValue = selectedRadioButton.getText();
        switch (toogleGroupValue) {
            case "Sine":
                waveform = SoundProductionSystem.Waveform.SIN;
                break;
            case "Square":
                waveform = SoundProductionSystem.Waveform.RECT;
                break;
            case "Triangle":
                waveform = SoundProductionSystem.Waveform.TRI;
                break;
            case "Sawtooth":
                waveform = SoundProductionSystem.Waveform.SAWTOOTH;
                break;

        }
        player.setWaveform(waveform);
    }

    private void playNote(Object source) {
        player.addNote(getTone(source), 1.0);

    }

    private void stopNote(Object source) {
        player.stopNote(getTone(source));
    }

}

