package be.manudahmen.mylittlesynth;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class NoteGrid extends TableView {
   private final int MAXCOLUMN = 16;

   public NoteGrid() {
      int i;
      for(i = 0; i < 16; ++i) {
         this.getColumns().add(new TableColumn("" + i));
      }

      for(i = 0; i < 16; ++i) {
         ((TableColumn)((TableColumn)this.getColumns().get(i))).setMaxWidth(1.0D);
      }

   }
}
