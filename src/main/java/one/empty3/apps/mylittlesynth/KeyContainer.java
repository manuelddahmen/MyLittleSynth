package one.empty3.apps.mylittlesynth;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
    private Note lastNote = null;
    private AppNew appNew;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    public KeyContainer(AppNew appNew)

    {
        this.appNew = appNew;
        initComponents();
        afterInit();
        soundProductionSystem = new SoundProductionSystem();
    }


    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public void setPropertyChangeSupport(PropertyChangeSupport propertyChangeSupport) {
        this.propertyChangeSupport = propertyChangeSupport;
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
        Note2 note = new Note2(tone,  appNew.getListInstruments().getSelectedInstrumentIndex());
        propertyChangeSupport.firePropertyChange("playNote", lastNote, note);
        lastNote = note;
        //player.playNote(note);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        //======== this ========
        setLayout(new MigLayout(
            "fill,hidemode 3",
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
    // Generated using JFormDesigner non-commercial license
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
