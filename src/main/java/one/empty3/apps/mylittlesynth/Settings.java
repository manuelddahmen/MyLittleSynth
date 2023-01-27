package one.empty3.apps.mylittlesynth;


import java.io.*;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Created by manue on 01-09-19.
 */
public class Settings {
    private Settings settings;
    private File recordingDir;

    private Settings(String defaultSettingsDirectory)
    {
        File file = new File(defaultSettingsDirectory);
        File conf = new File(file.getAbsolutePath() + "/" + "settings.conf");
        if(!file.exists())
        {
            file.mkdirs();

            if(!conf.exists())
            try {
                conf.createNewFile();
                PrintWriter out = new PrintWriter(conf);
                out.println("recordingsDir=./recordings/");
                out.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loadConf(conf);



        this.settings = this;
    }

    private void loadConf(File conf) {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(conf));
            this.recordingDir = new File(properties.getProperty("recordingsDir"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Settings getSettings()
    {
        ResourceBundle defaultSettings = ResourceBundle.getBundle("defaultSettings");
        String defaultSettingsDirectory = defaultSettings.getString("settingsDirectory");
        if(settings==null)
            settings = new Settings(defaultSettingsDirectory);
        return settings;
    }
}
