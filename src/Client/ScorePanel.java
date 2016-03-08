//package Client;
//
//import javax.swing.*;
//import java.awt.*;
//import java.util.Observable;
//import java.util.Observer;
//
//class ScorePanel extends JPanel implements Observer {
//    JLabel lbBlack = new JLabel("Black: ");
//    JLabel lbWhite = new JLabel("White: ");
//    JLabel countBlack = new JLabel("2");
//    JLabel countWhite = new JLabel("2");
//
//    ScorePanel() {
//        /****** components ******/
//        JPanel playerBlackPanel = new JPanel(); //PanelBabyInteger to show user black
//        JPanel playerWhitePanel = new JPanel(); //PanelBabyInteger to show user white
//
//
//        /****** layout ******/
//        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//
//        /****** dimension ******/
//        Dimension dScore = new Dimension(200, 100);
//        Dimension dBlack = new Dimension(200, 50);
//        Dimension dWhite = new Dimension(200, 50);
//        this.setPreferredSize(dScore);
//        playerBlackPanel.setPreferredSize(dBlack);
//        playerWhitePanel.setPreferredSize(dWhite);
//
//        /****** add ******/
//        this.add(playerBlackPanel);
//        this.add(playerWhitePanel);
//        playerBlackPanel.add(lbBlack);
//        playerBlackPanel.add(countBlack);
//        playerWhitePanel.add(lbWhite);
//        playerWhitePanel.add(countWhite);
//    }
//
//    @Override
//    public void update(Observable o, Object arg) {
//        if(arg.toString().startsWith("{\"score\"")){
//            System.out.println("Hello from ScorePanel");
//            countBlack.setText((String) arg);
//        }
//    }
//}
