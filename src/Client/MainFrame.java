package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class MainFrame extends JFrame{
    private static final Color BACKGROUND_COLOR = new Color(60, 150, 60);	//set background color;
    private StatusContainer panelStatus = new StatusContainer();
    private GamePanel gamePanel = new GamePanel();
    //Constructor
    public MainFrame() {
        this.setSize(700, 500);
        this.setBackground(BACKGROUND_COLOR);
        this.setVisible(true);
        this.add("East", panelStatus);
        this.add("Center", gamePanel);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
                //TODO: shutdown socket
            }
        });
    }

    //main method access
    public static void main(String[] args) {
        new MainFrame();
    }
}