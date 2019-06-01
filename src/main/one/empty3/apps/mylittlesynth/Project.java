package one.empty3.apps.mylittlesynth;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipFile;

public class Project {
   private HashMap entries = new HashMap();

   private Project() {
   }

   public static Project openProject(File file) {
      Project project = new Project();

      try {
         new ZipFile(file);
      } catch (IOException var3) {
         var3.printStackTrace();
      }

      return project;
   }

   public void save() {
   }
}
