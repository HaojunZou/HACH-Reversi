package Client;

import javax.swing.*;
import java.awt.*;
import java.net.*;

class StatusContainer extends JPanel {
    private String host;
    private int port;
    /****** components ******/
    private ScorePanel scorePanel = new ScorePanel();
    private ConnectionPanel connectionPanel = new ConnectionPanel();
    private DialogPanel dialogPanel = new DialogPanel();

    //Constructor
    StatusContainer() {
        /****** layout ******/
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        /****** dimension ******/
        Dimension dStatus = new Dimension(200, 500);
        this.setPreferredSize(dStatus);

        /****** add ******/
        this.add(scorePanel);
        this.add(connectionPanel);
        this.add(dialogPanel);
    }

}
