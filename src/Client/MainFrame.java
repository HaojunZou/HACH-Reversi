package Client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class MainFrame extends JFrame{
    private static final Color BACKGROUND_COLOR = new Color(60, 150, 60);	//set background color;

    //Constructor
    private MainFrame() {
        GamePanel gamePanel = new GamePanel();
        StatusPanel panelStatus = new StatusPanel();
        this.setSize(700, 500);
        this.setBackground(BACKGROUND_COLOR);
        this.setVisible(true);
        this.add("Center", gamePanel);
        this.add("East", panelStatus);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    //main method access
    public static void main(String[] args) {
        new MainFrame();
    }
}