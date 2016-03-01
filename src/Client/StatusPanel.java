package Client;

import javax.swing.*;

class StatusPanel extends JPanel {
    //Constructor
    StatusPanel(){
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        JLabel lbBlack = new JLabel("Black: ");
        JLabel lbWhite = new JLabel("White: ");
        JLabel countBlack = new JLabel("2");
        JLabel countWhite = new JLabel("2");
        this.add(lbBlack);
        this.add(countBlack);
        this.add(lbWhite);
        this.add(countWhite);
    }
}
