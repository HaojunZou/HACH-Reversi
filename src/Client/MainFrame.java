package Client;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class MainFrame extends JFrame{
    private Socket socket;
    private	BufferedReader bufferedReader;
    private	BufferedWriter bufferedWriter;
    private static final int BLACK = 1;     //player BLACK
    private static final int WHITE = -1;	//player WHITE
    private static final Color BACKGROUND_COLOR = new Color(60, 150, 60);	//set background color;
    private static final Color AVAILABLE_PLACE_COLOR = new Color(50, 50, 50);	//set available places color;
    private int myFace;
    private int[][] map = new int[8][8];	//main 2d-array to save all pieces
//    private MessageBoy messageBoy = new MessageBoy();
    private JSONObject jsonObject;
    private StatusContainer panelStatus = new StatusContainer();
    private GamePanel gamePanel = new GamePanel(map);

    /****** Status Container ******/
    private ScorePanel scorePanel = new ScorePanel();
//    private ConnectionPanel connectionPanel = new ConnectionPanel();
    private DialogPanel dialogPanel = new DialogPanel();

    /****** Score Panel ******/
    JLabel lbBlack = new JLabel("Black: ");
    JLabel lbWhite = new JLabel("White: ");
    JLabel countBlack = new JLabel("0");
    JLabel countWhite = new JLabel("0");
    JPanel playerBlackPanel = new JPanel();
    JPanel playerWhitePanel = new JPanel();

    /****** Dialog Panel ******/
    private JTextArea dialogArea = new JTextArea();
    private JButton btnReady = new JButton("Ready");

    //Constructor
    public MainFrame() {
        /****** Main Frame ******/
        this.setSize(700, 500);
        this.setBackground(BACKGROUND_COLOR);
        this.setVisible(true);
        this.add("East", panelStatus);
        this.add("Center", gamePanel);

        /****** Status Panel ******/
        panelStatus.setLayout(new BoxLayout(panelStatus, BoxLayout.Y_AXIS));
        Dimension dStatus = new Dimension(200, 500);
        panelStatus.setPreferredSize(dStatus);
        panelStatus.add(scorePanel);
//        panelStatus.add(connectionPanel);
        panelStatus.add(dialogPanel);

        /****** Score Panel ******/
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        Dimension dScore = new Dimension(200, 100);
        Dimension dBlack = new Dimension(200, 50);
        Dimension dWhite = new Dimension(200, 50);
        scorePanel.setPreferredSize(dScore);
        playerBlackPanel.setPreferredSize(dBlack);
        playerWhitePanel.setPreferredSize(dWhite);
        scorePanel.add(playerBlackPanel);
        scorePanel.add(playerWhitePanel);
        playerBlackPanel.add(lbBlack);
        playerBlackPanel.add(countBlack);
        playerWhitePanel.add(lbWhite);
        playerWhitePanel.add(countWhite);

        /****** Dialog Panel ******/
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
        Dimension dDialog = new Dimension(200, 25);
        Dimension dReady = new Dimension(200, 25);
        Dimension dText = new Dimension(200, 300);
        dialogPanel.setPreferredSize(dDialog);
        btnReady.setPreferredSize(dReady);
        dialogArea.setPreferredSize(dText);
        dialogPanel.add(btnReady);
        dialogPanel.add(dialogArea);
        dialogArea.setEditable(false);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    socket.close();
                    bufferedReader.close();
                    bufferedWriter.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
                //TODO: shutdown socket
            }
        });
        launch();
    }

    private void launch(){
        try {
            socket = new Socket("127.0.0.1", 8888);
            btnReady.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if(btnReady.isEnabled()) {
                        sendMessage(socket, "command", "ready");
                    }
                    btnReady.setEnabled(false);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        getMessage(socket);
    }

    //main method access
    public static void main(String[] args) {
        new MainFrame();
    }

    class GamePanel extends JPanel implements MouseListener{
        int [][] piece = new int [8][8];
        //Constructor
        GamePanel(int[][] piece){
            this.piece = piece;
            Dimension dMap = new Dimension(500, 500);
            addMouseListener(this);
            this.setBackground(BACKGROUND_COLOR);
            this.setPreferredSize(dMap);
        }

        /**
         * draw map
         */
        private void drawMap(Graphics g) {
            //draw pieces
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (piece[i][j] != 0) {	//if there's piece in place
                        if (piece[i][j] == 1) {             //BLACK
                            g.setColor(Color.BLACK);
                            g.fillOval(55 + 50 * i, 55 + 50 * j, 40, 40);
                        } else if (piece[i][j] == -1) { 	//WHITE
                            g.setColor(Color.WHITE);
                            g.fillOval(55 + 50 * i, 55 + 50 * j, 40, 40);
                        } else if (piece[i][j] == 2) { 	    //available place
                            g.setColor(AVAILABLE_PLACE_COLOR);
                            g.fillOval(72 + 50 * i, 72 + 50 * j, 6, 6);
                        }
                    }
                }
            }
        }

        private void drawPiece(int i, int j) {
            Graphics g = this.getGraphics();
            if (piece[i][j] == BLACK) {
                g.setColor(Color.BLACK);
            } else if (piece[i][j] == WHITE) {
                g.setColor(Color.WHITE);
            }
            g.fillOval(55 + 50 * i, 55 + 50 * j, 40, 40);
        }

        @Override
        public void paint(Graphics g) {	//override the paint method
            super.paint(g);
            //initial player color in the corner
//            if(algorithm.curPiece() == 1) {
//                g.setColor(Color.BLACK);
//                g.fillOval(5, 5, 40, 40);
//            }
//            if(algorithm.curPiece() == -1) {
//                g.setColor(Color.WHITE);
//                g.fillOval(5, 5, 40, 40);
//            }

            //draw horizontal lines
            for (int i = 0; i <= 8; i++) {
                g.drawLine(50, 50 * i + 50, 450, 50 * i + 50);
            }
            //draw vertical lines
            for (int i = 0; i <= 8; i++) {
                g.drawLine(50 * i + 50, 50, 50 * i + 50, 450);
            }
            //draw mark
            String[] xl = { "A", "B", "C", "D", "E", "F", "G", "H" };
            String[] yl = { "1", "2", "3", "4", "5", "6", "7", "8" };
            for (int i = 0; i < yl.length; i++) {
                g.drawString(xl[i], 50 * i + 72, 40);
                g.drawString(yl[i], 40, 50 * i + 80);
            }
            drawMap(g);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int [] position = new int[2];
            int x = e.getX();	//get the x-coordinate when mouse clicked
            int y = e.getY();	//get the y-coordinate when mouse clicked
            // get the grille position as index
            int i = (x - 50) / 50;
            int j = (y - 50) / 50;
            position[0] = i;
            position[1] = j;
            System.out.println("Player is at: (" + i + ", " + j + ")");	// the index
            if (i < 0 || i >= 8 || j < 0 || j >= 8)
                System.out.println("Place out of range");
            else{
//                algorithm.move(i, j);
                sendMessage(socket, "move", position);
//                drawPiece(i, j);
            }
        }
        @Override
        public void mousePressed(MouseEvent e) {}
        @Override
        public void mouseReleased(MouseEvent e) {}
        @Override
        public void mouseEntered(MouseEvent e) {}
        @Override
        public void mouseExited(MouseEvent e) {}
    }

    class StatusContainer extends JPanel {
        //Constructor
        StatusContainer() {
        }
    }

    class ScorePanel extends JPanel {
        ScorePanel() {
        }
    }

    class DialogPanel extends JPanel{
        DialogPanel() {
        }
    }

    class ConnectionPanel extends JPanel {
    }

    private void getMessage(final Socket clientSocket){
        new Thread(){
            @Override
            public void run() {
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                    bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                    String text;
                    while(true) {
                        if ((text = bufferedReader.readLine()) != null) {
                            jsonObject = new JSONObject(text);
                            if (text.contains("\"show\"")) {
                                String mapString = jsonObject.get("show").toString();
                                System.out.println(mapString);
                                System.out.println("Ok, update");
                                refreshMap(mapString);
                            } else if (text.contains("\"message\"")) {
                                dialogArea.append(jsonObject.get("message").toString() + "\n");
                            } else if (text.contains("\"score\"")) {
                                countBlack.setText(jsonObject.get("score").toString().replace("[", "").replace("]", "").split(",")[0]);
                                countWhite.setText(jsonObject.get("score").toString().replace("[", "").replace("]", "").split(",")[1]);
                            } else if(text.contains("\"color\"")){
                                myFace = Integer.parseInt(jsonObject.get("color").toString());
                                System.out.println(myFace);
                            }
                            else
                                break;
                        }
                        else
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void sendMessage(final Socket clientSocket, String key, Object value){
        new Thread(){
            @Override
            public void run() {
                try {
                    if(clientSocket.isConnected()) {
                        jsonObject = new JSONObject();
                        jsonObject.put(key, value);
                        bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                        String jsonString = jsonObject.toString();
                        bufferedWriter.write(jsonString);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public int[][] refreshMap(String jsonString){
        int[][] newMap = new int [8][8];
        new Thread(){
            @Override
            public void run() {
                String [] stringArray = jsonString.replace("[", "").replace("]", "").split(",");

                for(int y=0; y<8; y++){
                    for(int x=0; x<8; x++){
                        newMap[x][y] = Integer.parseInt(stringArray[y + (x << 3)]);
                    }
                }
                MainFrame.this.remove(gamePanel);
                GamePanel gamePanel = new GamePanel(newMap);
                MainFrame.this.add("Center", gamePanel);
                gamePanel.updateUI();
                System.out.println(Arrays.deepToString(newMap));
            }
        }.start();
        return newMap;
    }
}