/*
 * Created by JFormDesigner on Thu Oct 10 17:56:29 CEST 2019
 */

package one.empty3.apps.mylittlesynth;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

/**
 * @author Manuel Dahmen
 */
public class AppNew extends JFrame implements PropertyChangeListener{
    private final PlayerSwing playerSwing;
    private KeyContainer keyContainer;
    private Rec recordind = new Rec();


    public AppNew() {
        initComponents();

        playerSwing = new PlayerSwing(this);
        keyContainer1.setPlayer(playerSwing);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        playerSwing.start();
        keyContainer1.getPropertyChangeSupport().addPropertyChangeListener(this);
    }


    public static void main(String [] arfs)
    {
        AppNew appNew = new AppNew();
        appNew.setVisible(true);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("one.empty3.apps.mylittlesynth.bundle");
        tabbedPane1 = new JTabbedPane();
        panel1 = new JPanel();
        splitPane1 = new JSplitPane();
        keyContainer1 = new KeyContainer(this);
        instrumentChooser1 = new InstrumentChooser(this);

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new MigLayout(
            "hidemode 3",
            // columns
            "[fill]" +
            "[fill]" +
            "[fill]",
            // rows
            "[]" +
            "[]" +
            "[]"));

        //======== tabbedPane1 ========
        {

            //======== panel1 ========
            {
                panel1.setLayout(new MigLayout(
                    "hidemode 3",
                    // columns
                    "[fill]" +
                    "[fill]" +
                    "[fill]",
                    // rows
                    "[]" +
                    "[]" +
                    "[]"));

                //======== splitPane1 ========
                {
                    splitPane1.setLeftComponent(keyContainer1);

                    //---- instrumentChooser1 ----
                    instrumentChooser1.setMaximumSize(new Dimension(100, 400));
                    splitPane1.setRightComponent(instrumentChooser1);
                }
                panel1.add(splitPane1, "cell 0 1");
            }
            tabbedPane1.addTab(bundle.getString("AppNew.panel1.tab.title"), panel1);
        }
        contentPane.add(tabbedPane1, "cell 0 1");

        initComponentsI18n();

        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getNewValue()!=null)
        {
            Note2 newValue = (Note2) evt.getNewValue();
            playerSwing.playNote(newValue);
            recordind.addNote(newValue);
        }
    }

    public KeyContainer getKeyContainer() {
        return keyContainer1;
    }

    public InstrumentChooser getListInstruments() {
        return instrumentChooser1;
    }
    private void initComponentsI18n() {
        // JFormDesigner - Component i18n initialization - DO NOT MODIFY  //GEN-BEGIN:initI18n
        ResourceBundle bundle = ResourceBundle.getBundle("one.empty3.apps.mylittlesynth.bundle");
        tabbedPane1.setTitleAt(0, bundle.getString("AppNew.panel1.tab.title"));
        // JFormDesigner - End of component i18n initialization  //GEN-END:initI18n
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private JSplitPane splitPane1;
    private KeyContainer keyContainer1;
    private InstrumentChooser instrumentChooser1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
