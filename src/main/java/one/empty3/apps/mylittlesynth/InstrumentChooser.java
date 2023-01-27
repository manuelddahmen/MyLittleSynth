package one.empty3.apps.mylittlesynth;

import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.beans.*;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;


/**
 * Created by manue on 10-10-19.
 */
public class InstrumentChooser extends JPanel {
    private AppNew appNew;
    private Instrument selectedInstrument;
    private int selectedInstrumentindex =  01;
    public InstrumentChooser()
    {
        initComponents();

        try {
            Instrument[] availableInstruments = MidiSystem.getSynthesizer().getAvailableInstruments();
            table1.setModel(new DefaultTableModel(availableInstruments.length, 2));
            int i = 0;
            for (Instrument availableInstrument : availableInstruments) {

                table1.getModel().setValueAt(availableInstrument, i, 0);
                table1.getModel().setValueAt(availableInstrument.getName(), i, 1);

                i++;
            }
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    public InstrumentChooser(AppNew appNew) {
        this();
        this.appNew = appNew;
    }

    public Instrument getSelectedInstrument() {
        return selectedInstrument;
    }
    public int getSelectedInstrumentIndex() {
        selectedInstrumentindex = table1.getSelectedRow();
        System.out.println("Instruemnt index: " + selectedInstrumentindex);
        return selectedInstrumentindex;
    }

    public void setSelectedInstrument(Instrument selectedInstrument) {
        this.selectedInstrument = selectedInstrument;
    }

    private void table1PropertyChange(PropertyChangeEvent e) {
        System.out.println(e.getPropertyName());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        scrollPane1 = new JScrollPane();
        table1 = new JTable();

        //======== this ========
        setLayout(new MigLayout(
            "fill,hidemode 3",
            // columns
            "[fill]",
            // rows
            "[]" +
            "[]"));

        //======== scrollPane1 ========
        {

            //---- table1 ----
            table1.setPreferredScrollableViewportSize(new Dimension(450, 800));
            table1.addPropertyChangeListener(e -> table1PropertyChange(e));
            scrollPane1.setViewportView(table1);
        }
        add(scrollPane1, "cell 0 1,dock center");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JScrollPane scrollPane1;
    private JTable table1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
