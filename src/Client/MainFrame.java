package Client;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class MainFrame extends JFrame implements MessageBoy{
    private Socket socket = null;
    private int[][] map = new int[8][8];	//main 2d-array to save all pieces
    private boolean inGame = false;
    private int currentPlayer = 1;
    private GamePanel gamePanel = new GamePanel(map);
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;

    private static final Color BACKGROUND_COLOR = new Color(60, 150, 60);
    private static final Color AVAILABLE_PLACE_COLOR = new Color(50, 50, 50);
    private static final Color BLACK_COLOR = new Color(0, 0, 0);
    private static final Color WHITE_COLOR = new Color(255, 255, 255);

    /****** Score Panel ******/
    private JLabel lbBlack = new JLabel("Black: "); //TODO: change to icon later on
    private JLabel lbWhite = new JLabel("White: "); //TODO: change to icon later on
    private JLabel countBlack = new JLabel("0");
    private JLabel countWhite = new JLabel("0");

    /****** Dialog Panel ******/
    private JTextArea dialogArea = new JTextArea();
    private JButton btnReady = new JButton("Ready");
    private JButton btnNotReady = new JButton("Not Ready");
    private JButton btnSurrender = new JButton("Surrender");

    //Constructor
    private MainFrame() {
        /****** Main Frame ******/
        this.setTitle("HACH-Reversi");
        this.setSize(700, 525);
        this.setBackground(BACKGROUND_COLOR);
        this.setVisible(true);
        this.setResizable(false);
        JPanel panelStatus = new JPanel();
        this.add("East", panelStatus);
        this.add("Center", gamePanel);

        /****** Status Panel ******/
        panelStatus.setLayout(new BoxLayout(panelStatus, BoxLayout.Y_AXIS));
        Dimension dStatus = new Dimension(200, 525);
        panelStatus.setPreferredSize(dStatus);

        /***** Status Container ******/
        JPanel scorePanel = new JPanel();
        ConnectionPanel connectionPanel = new ConnectionPanel();
        JPanel buttonPanel = new JPanel();
        JPanel dialogPanel = new JPanel();
        panelStatus.add(scorePanel);
        panelStatus.add(connectionPanel);
        panelStatus.add(buttonPanel);
        panelStatus.add(dialogPanel);

        /****** Score Panel ******/
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        Dimension dScore = new Dimension(200, 100);
        Dimension dBlack = new Dimension(200, 50);
        Dimension dWhite = new Dimension(200, 50);
        scorePanel.setPreferredSize(dScore);
        JPanel playerBlackPanel = new JPanel();
        playerBlackPanel.setPreferredSize(dBlack);
        JPanel playerWhitePanel = new JPanel();
        playerWhitePanel.setPreferredSize(dWhite);
        scorePanel.add(playerBlackPanel);
        scorePanel.add(playerWhitePanel);
        playerBlackPanel.add(lbBlack);
        playerBlackPanel.add(countBlack);
        playerWhitePanel.add(lbWhite);
        playerWhitePanel.add(countWhite);

        /****** Dialog Panel ******/
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        Dimension dButton = new Dimension(200, 25);
        Dimension dScroll = new Dimension(200, 300);
        JScrollPane dialogScroll = new JScrollPane(dialogArea);
        dialogScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        buttonPanel.setPreferredSize(dButton);
        dialogScroll.setPreferredSize(dScroll);
        buttonPanel.add(btnReady);
        buttonPanel.add(btnNotReady);
        buttonPanel.add(btnSurrender);
        btnNotReady.setVisible(false);
        btnSurrender.setEnabled(false);
        dialogPanel.add(dialogScroll);
        dialogArea.setEditable(false);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    sendMessage("quit", "yes");
                    System.exit(0);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }finally {
                    try { if(bufferedReader != null) bufferedReader.close(); } catch (IOException e1) { e1.printStackTrace(); }
                    try { if(bufferedWriter != null) bufferedWriter.close(); } catch (IOException e1) { e1.printStackTrace(); }
                    try { if(socket != null) socket.close(); } catch (IOException e1) { e1.printStackTrace(); }
                }
            }
        });
        launch();
        getMessage();
    }

    //main method access
    public static void main(String[] args) {
        new MainFrame();
    }

    private void launch(){
        try {
            socket = new Socket("127.0.0.1", 8888);
            btnReady.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    sendMessage("command", "ready");
                }
            });
            btnNotReady.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    sendMessage("command", "notReady");
                    btnNotReady.setVisible(false);
                    btnReady.setVisible(true);
                }
            });
            btnSurrender.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    int confirm;
                    if(inGame) {
                        confirm = JOptionPane.showConfirmDialog(null, "Are you sure to surrender?\nYour rival will win this game automatically!", "No", JOptionPane.YES_NO_OPTION);
                        if (confirm == 0) {
                            sendMessage("command", "surrender");
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getMessage(){
        new Thread(){
            @Override
            public void run() {
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    String command;
                    while ((command = bufferedReader.readLine()) != null) {
                        Command(command);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void sendMessage(String key, Object value){
        try {
            if(socket.isConnected()) {
                JSONObject jsonSend = new JSONObject();
                jsonSend.put(key, value);
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                String jsonString = jsonSend.toString();
                bufferedWriter.write(jsonString);
                bufferedWriter.write("\r\n");
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Command(String cmd) {
        JSONObject jsonGet = new JSONObject(cmd);
        try {
            if (cmd.contains("\"show\":")) {
                String mapString = jsonGet.get("show").toString();
                refreshMap(mapString);
            } else if (cmd.contains("\"message\":")) {
                dialogArea.append(jsonGet.get("message").toString() + "\n");
                dialogArea.setCaretPosition(dialogArea.getText().length());
            } else if (cmd.contains("\"score\":")) {
                countBlack.setText(jsonGet.get("score").toString().replace("[", "").replace("]", "").split(",")[0]);
                countWhite.setText(jsonGet.get("score").toString().replace("[", "").replace("]", "").split(",")[1]);
            } else if(cmd.contains("\"game\":")){
                if(jsonGet.get("game").toString().equals("on")) {
                    btnReady.setVisible(false);
                    btnNotReady.setVisible(false);
                    btnSurrender.setEnabled(true);
                    inGame = true;
                } else if(jsonGet.get("game").toString().equals("off")) {
                    inGame = false;
                    btnReady.setVisible(true);
                    btnNotReady.setVisible(false);
                    btnSurrender.setEnabled(false);
                } else if(jsonGet.get("game").toString().equals("ready")) {
                    inGame = false;
                    btnReady.setVisible(false);
                    btnNotReady.setVisible(true);
                    btnSurrender.setEnabled(false);
                } else if(jsonGet.get("game").toString().equals("wait")) {
                    inGame = false;
                    btnReady.setVisible(true);
                    btnNotReady.setVisible(false);
                    btnSurrender.setEnabled(false);
                }
            } else if(cmd.contains("\"current\":")){
                if (jsonGet.get("current").toString().equals("1"))
                    currentPlayer = 1;
                else if (jsonGet.get("current").toString().equals("-1"))
                    currentPlayer = -1;
            } else if(cmd.contains("\"warning\":")){
                JOptionPane.showMessageDialog(null, jsonGet.get("warning").toString());
            }
        }catch (Exception e){
            System.out.println("Unknown command");
        }
    }

    private int[][] refreshMap(String jsonString){
        int[][] newMap = new int [8][8];
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
        return newMap;
    }

    private class GamePanel extends JPanel implements MouseListener{
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
                            g.setColor(BLACK_COLOR);
                            g.fillOval(55 + 50 * i, 55 + 50 * j, 40, 40);
                        } else if (piece[i][j] == -1) { 	//WHITE
                            g.setColor(WHITE_COLOR);
                            g.fillOval(55 + 50 * i, 55 + 50 * j, 40, 40);
                        } else if (piece[i][j] == 2) { 	    //available place
                            g.setColor(AVAILABLE_PLACE_COLOR);
                            g.fillOval(72 + 50 * i, 72 + 50 * j, 6, 6);
                        }
                    }
                }
            }
        }

        @Override
        public void paint(Graphics g) {	//override the paint method
            super.paint(g);
            //indicator shows current player color in the corner
            if(inGame) {
                if (currentPlayer == 1) {
                    g.setColor(BLACK_COLOR);
                    g.fillOval(5, 5, 40, 40);
                }
                if (currentPlayer == -1) {
                    g.setColor(WHITE_COLOR);
                    g.fillOval(5, 5, 40, 40);
                }
            }

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
                if(inGame)
                    sendMessage("move", position);
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

}