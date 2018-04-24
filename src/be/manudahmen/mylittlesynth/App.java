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
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.HashMap;
import java.util.Map;

public class App extends Application {
    private Slider slider;
    private SoundProductionSystem soundProductionSystem;
    private ToggleGroup group;
    private Player player;
    private AudioViewer audioViewer;
    private Map<Integer, Note> noteMap;
    private double minDuration = 2.0;
    private Slider volume;

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

        BorderPane bp = new BorderPane();
        BorderPane bp2 = new BorderPane();
        bp.setTop(pane[0]);
        bp.setCenter(pane[1]);


        String[] notes = new String[]{
                "do", "re", "mi", "fa", "sol", "la", "si", "do",
                "do#", "re#", "--", "fa#", "sol#", "la#", "--", "do#"};

        Canvas canvas = new Canvas(640, 480);
        canvas.setLayoutX(640);
        canvas.setLayoutY(480);
        Pane pane1 = new Pane();

        pane1.getChildren().add(canvas);


        slider = new Slider();
        slider.setMin(0);
        slider.setMax(12);
        slider.setValue(4);
        slider.setMinorTickCount(1);
        slider.setAccessibleText("Octaves");
        slider.setOnMouseReleased(event -> {
            int value = (int) ((Slider) event.getSource()).getValue();
            player.setOctave(value);
            System.out.println("Octave: " + value);

        });
        //player.setOctave((int)slider.getValue());
        noteMap = new HashMap<>();

        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new Button(notes[i]);
            buttons[i].setLayoutX(100 + i * 30);
            buttons[i].setLayoutY(150 + (i >= 8 ? 30 : 0));
            buttons[i].setId("Button" + i);
            buttons[i].arm();
            //noteMap.put(getTone(buttons[i]), new Note(minDuration, getTone(buttons[i]),
            //        SoundProductionSystem.Waveform.SIN, new Enveloppe(minDuration)));


            pane[i < 8 ? 0 : 1].getChildren().add(buttons[i]);
        }

        for (int i = 0; i < 8 * 12; i++) {
            noteMap.put(i,
                    new Note(minDuration, i,
                            SoundProductionSystem.Waveform.SIN,
                            new Enveloppe(minDuration)));

        }
        KeyCode[] keycode = new KeyCode[]{KeyCode.A, KeyCode.Z, KeyCode.E,
                KeyCode.R, KeyCode.T, KeyCode.Y, KeyCode.U, KeyCode.I,

                KeyCode.Q, KeyCode.S, KeyCode.D, KeyCode.F, KeyCode.G, KeyCode.H, KeyCode.J, KeyCode.K};


        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setOnMousePressed(event -> playNote((Button) event.getSource()));
            buttons[i].setOnMouseReleased(event -> stopNote((Button) event.getSource()));
            buttons[i].setOnTouchPressed(event -> playNote((Button) event.getSource()));
            buttons[i].setOnTouchReleased(event -> stopNote((Button) event.getSource()));
        }

        VBox vBox = new VBox();

        group = new ToggleGroup();

        RadioButton radioButton = new RadioButton();
        radioButton.setText("Sine");
        radioButton.setToggleGroup(group);
        radioButton.setSelected(true);
        radioButton.setOnAction(event -> {
            String value = ((RadioButton) event.getSource()).getText();
            player.setForm(value);
        });
        vBox.getChildren().add(radioButton);
        radioButton = new RadioButton();
        radioButton.setText("Square");
        radioButton.setToggleGroup(group);
        radioButton.setOnAction(event -> {
            String value = ((RadioButton) event.getSource()).getText();
            player.setForm(value);
        });
        vBox.getChildren().add(radioButton);
        radioButton = new RadioButton();
        radioButton.setText("Triangle");
        radioButton.setToggleGroup(group);
        radioButton.setOnAction(event -> {
            String value = ((RadioButton) event.getSource()).getText();
            player.setForm(value);
        });
        vBox.getChildren().add(radioButton);
        radioButton = new RadioButton();
        radioButton.setText("Sawtooth");
        radioButton.setToggleGroup(group);
        radioButton.setOnAction(event -> {
            String value = ((RadioButton) event.getSource()).getText();
            player.setForm(value);
        });
        vBox.getChildren().add(radioButton);


        bp.setLeft(vBox);


        bp.setRight(slider);


        bp.setBottom(pane1);

        bp2.setTop(bp);
        bp2.setCenter(canvas);

        TitledPane[] titledPanes = new TitledPane[2];

        titledPanes[0] = new TitledPane();


        titledPanes[0].setOnKeyPressed(event -> {
            for (int i = 0; i < buttons.length; i++) {
                if (event.getCode().equals(keycode[i])) {
                    playNote(buttons[i]);
                    System.out.println("playNote" + player.getCurrentNotes().size());
                }
            }
        });
        titledPanes[0].setOnKeyReleased(event -> {
            for (int i = 0; i < buttons.length; i++) {
                if (event.getCode().equals(keycode[i])) {
                    stopNote(buttons[i]);
                    System.out.println("stopNote" + player.getCurrentNotes().size());
                }
            }
        });


        titledPanes[0].setContent(bp2);

        titledPanes[1] = new TitledPane();
        titledPanes[1].setContent(new NoteGrid());

        Accordion accordion = new Accordion();

        accordion.getPanes().addAll(titledPanes);

        scene.setRoot(accordion);

        accordion.setExpandedPane(titledPanes[0]);


        bp2.setBottom(volume = new Slider());
        volume.setMin(0);
        volume.setMax(100);
        volume.setValue(100.0);
        volume.setAccessibleText("Volume:" + volume.getValue());
        volume.setOnMouseReleased(event -> {
            player.setVolume(volume.getValue());
            System.out.println("Volume du player: " + player.getVolume());

        });

        primaryStage.setTitle("Plants 2.0 synth");
        primaryStage.setScene(scene);


        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                player.setPlaying(false);
                audioViewer.setRunning(false);
                System.exit(0);
            }
        });
        audioViewer = new AudioViewer(44100, 2, canvas);
        player = new Player(audioViewer);
        player.setVolume(100);
        player.start();

/*
        for (int i = 0; i < buttons.length; i++) {
            Button b = buttons[i];
            b.getScene().getAccelerators().put(new KeyCodeCombination(keycode[i]), () -> fireButton(b));
        }
*/
    }

    // fires a button from code, providing visual feedback that the button is firing.
    /*private void fireButton(final Button button) {
        button.arm();
        PauseTransition pt = new PauseTransition(Duration.millis(300));
        pt.setOnFinished(event -> {
            button.fire();

            button.disarm();
        });
        pt.play();
    }*/
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

        int octave = (int) slider.getValue();//player.getOctave();
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

    private void playNote(Button source) {
        System.out.println("Tone:  " + getTone(source) + "  Note :"
                + ((Button) source).getText() + "  "
                + "Octave: " + (int) slider.getValue());
        Note note = noteMap.get(getTone(source));
        assert note != null;
        note.setWaveform(player.getForm());
        assert note != null;
        player.playNote(note);

    }

    private void stopNote(Button source) {
        Note note = noteMap.get(getTone(source));
        assert note != null;
        System.out.println(note.getTimer().getTotalTimeElapsed());

        player.stopNote(note);
        System.out.println("Key pressed: " + player.getCurrentNotes().size());
    }

}

