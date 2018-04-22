package be.manudahmen.mylittlesynth;


import com.sun.javafx.scene.control.TableColumnComparatorBase;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class NoteGrid extends TableView {
    private final int MAXCOLUMN = 16;

    public NoteGrid() {
        super();
        for (int i = 0; i < MAXCOLUMN; i++) {
            getColumns().add(new TableColumn("" + i));
        }
        for (int i = 0; i < MAXCOLUMN; i++)
            ((TableColumn) (getColumns().get(i))).setMaxWidth(1.0);

    }
}
