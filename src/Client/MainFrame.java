package Client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class MainFrame extends JFrame{
    private static final Color BACKGROUND_COLOR = new Color(60, 150, 60);	//set background color;
    private JFrame frame = new JFrame();
    private GamePanel gamePanel = new GamePanel();
    private StatusPanel panelStatus = new StatusPanel();	//status

    //Constructor
    private MainFrame() {
        super();
        //style frame and components
        frame.setSize(700, 500);
        frame.setBackground(BACKGROUND_COLOR);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.add("Center", gamePanel);
        frame.add("East", panelStatus);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
                frame.dispose();
                System.exit(0);
            }
        });
    }

    //main method access
    public static void main(String[] args) {
        new MainFrame();
    }
}