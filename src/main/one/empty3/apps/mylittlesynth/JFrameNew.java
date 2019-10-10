package one.empty3.apps.mylittlesynth;

import javax.swing.*;

/**
 * Created by manue on 10-10-19.
 */
public class JFrameNew extends JFrame {
    public JFrameNew()
    {
        super("MyLittleSynth");
        KeyContainer keyContainer = new KeyContainer();
        setContentPane(keyContainer);
        PlayerSwing playerSwing = new PlayerSwing(keyContainer);
        keyContainer.setPlayer(playerSwing);
        pack();

        playerSwing.start();
    }


    public static void main(String [] args)
    {
        new JFrameNew().setVisible(true);
    }
}
