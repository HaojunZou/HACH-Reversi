package Client;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

class DialogPanel extends JPanel implements Observer {
    private String host;
    private int port;
    private JTextArea dialogArea;

    DialogPanel() {

        /****** components ******/
        dialogArea = new JTextArea();
        Dimension dDialog = new Dimension(200, 325);
        this.setPreferredSize(dDialog);
        this.add(dialogArea);
        dialogArea.setBackground(Color.white);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(arg.equals("message"))
            System.out.println("Hello from DialogPanel");
    }
}
