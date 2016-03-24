package Client;

import javax.swing.*;
import java.io.*;

public class Help extends JFrame{
    public Help(){
        this.setVisible(true);
        this.setSize(700, 500);
        this.setTitle("Help");
        this.setResizable(false);
        JTextArea helpText = new JTextArea();

        helpText.setText("===== How to start =====\n" +
                "1. Enter the server address and port number 20000\n" +
                "2. Click \"Connect\" and then \"Ready\"\n" +
                "3. Now wait for a second player to connect\n" +
                "4. If there is another player ready, then a game will be started\n" +
                "\n" +
                "\n" +
                "====== How to Play ======\n" +
                "1. Each game has only two players. Black and White\n" +
                "2. The starting position consists of two black bricks and two white bricks\n" +
                "3. Black does always start, and it is player 1\n" +
                "4. You can play a brick when you flank one or more opponents bricks between your new brick and\n" +
                "   any other of your own bricks, in the same horizontal, vertical or diagonal line.\n" +
                "   The opponents bricks that are flanked will be turned upside-down and change colour.\n" +
                "5. When there is no possible legal move, the turn is given back to the opponent\n" +
                "6. If both players need to pass because there is no possible move for any of them, the board game has ended.\n" +
                "   Normally, this will occur when there are no more empty squares\n" +
                "7. Whoever has the most bricks wins the board game. Empty squares are added towards the winners amount\n" +
                "\n" +
                "\n" +
                "====== How to Chat ======\n" +
                "1. In the right bottom corner you can see a text box\n" +
                "2. All you have to do is to write anything you want and then click \"send\" to chat with your opponent\n" +
                "\n" +
                "\n" +
                "====== To End the Game ======\n" +
                "1. If you give up, click \"Surrender\"\n" +
                "2. Otherwise the game ends when no one can lay any more bricks\n");


//        String path = new File("").getAbsolutePath() + "/Help.txt";
//        File file = new File(path);
//
//        try {
//            FileReader fr = new FileReader(file);
//            BufferedReader bw = new BufferedReader(fr);
//            String line;
//            while((line=bw.readLine()) != null) {
//                helpText.append(line + "\n");
//            }
//            fr.close();
//            bw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        helpText.setEditable(false);
        this.add(helpText);
    }
}
