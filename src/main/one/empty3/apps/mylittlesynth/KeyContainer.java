package one.empty3.apps.mylittlesynth;

import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import net.miginfocom.swing.*;
import one.empty3.apps.mylittlesynth.processor.WaveForm;

/**
 * Created by manue on 10-10-19.
 */
public class KeyContainer extends JPanel {
    private SoundProductionSystem soundProductionSystem;
    private PlayerSwing player;

    public KeyContainer()

    {
        initComponents();
        afterInit();
        soundProductionSystem = new SoundProductionSystem();
    }
    public String note(int i)
    {
        String [] array = new String [] {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
        return array[i];
    }

    public void afterInit()
    {
        for(int j=1; j<13; j++) { // Octaves
                for(int i=0; i<13; i++) { // Lines
                    JButton button1 = new JButton();
                    //---- button i,j ----
                    if(i==12) {
                        button1.setText("Oct:"+j);
                    } else {
                        button1.setText(note(i)+":"+j);
                        button1.addActionListener(new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                String []  s = ((JButton)e.getSource()).getText().split(":");
                                int tone = new SoundProductionSystem().equiv(s[0]+s[1]);
                                System.out.println("Note: "+s[0]+"\nOctave:"+s[1]+"\nTone:"+tone);
                                playNote(tone);
                            }
                        });
                    }
                        add(button1, "cell " + i + " " + j);
                }
        }
    }

    private void playNote(int tone) {
        player.playNote(new Note(null, 5.0, tone, WaveForm.SIN));
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        //======== this ========
        setLayout(new MigLayout(
            "fill,novisualpadding,hidemode 3",
            // columns
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]" +
            "[fill]",
            // rows
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]" +
            "[]"));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public SoundProductionSystem getSoundProductionSystem() {
        return soundProductionSystem;
    }

    public void setPlayer(PlayerSwing player) {
        this.player = player;
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
