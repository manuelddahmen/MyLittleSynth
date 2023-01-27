set JAVA_HOME=C:\\Program Files\\Java\\jdk-19
set PATH=%PATH%;C:\\Program Files\\Gradle\\bin
REM "%JAVA_HOME%\bin\java.exe" -Dfile.encoding=UTF-8 -classpath ".\lib\empty3-67.2.jar;.\lib\javalayer.jar;.\lib\montemedia-0.7.7.jar;.\lib\tritonus_mp3.jar;.\lib\tritonus_share-0.3.6;.\lib\xuggler-3.4.jar;target\mylittlesynth-2023.1.jar" one.empty3.apps.mylittlesynth.AppNew
REM java -cp target/mylittlesynth-2023.1.jar one.empty3.apps.mylittlesynth.AppNew
REM gradle --module-path lib/javafx-sdk-19.0.2.1/lib --add-modules javafx.fxml,javafx.controls,javafx.graphics,javafx.media runAppGui
gradle --stacktrace clean build distZip runNewAppGui