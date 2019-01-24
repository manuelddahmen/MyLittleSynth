package be.manudahmen.mylittlesynth.rythms;

import java.io.File;
import javafx.scene.control.ListCell;

public class FileCell extends ListCell {
   private File file;

   public FileCell(File file) {
      this.file = file;
   }
}
