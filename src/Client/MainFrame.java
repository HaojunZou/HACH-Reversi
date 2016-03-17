package Client;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class MainFrame extends JFrame implements MessageBoy{
    private Socket socket = null;
    private String host;
    private int port;
    private boolean connected = false;
    private int[][] map = new int[8][8];
    private boolean inGame = false;
    private int currentPlayer = 1;
    private GamePanel gamePanel = new GamePanel(map);
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;

    private static final Color BACKGROUND_COLOR = new Color(60, 150, 60);
    private static final Color AVAILABLE_PLACE_COLOR = new Color(50, 50, 50);
    private static final Color BLACK_COLOR = new Color(0, 0, 0);
    private static final Color WHITE_COLOR = new Color(255, 255, 255);

    /****** Status container ******/
    JPanel scorePanel = new JPanel();
    JPanel connectionPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JPanel dialogPanel = new JPanel();

    /****** Score Panel ******/
    private JLabel lbBlack = new JLabel("Black: ");
    private JLabel lbWhite = new JLabel("White: ");
    private JLabel countBlack = new JLabel("0");
    private JLabel countWhite = new JLabel("0");

    /****** Connection Panel ******/
    private JPanel hostPanel = new JPanel();
    private JPanel portPanel = new JPanel();
    private JPanel btnPanel = new JPanel();
    private JLabel lblHost = new JLabel("Host: ");
    private JTextField txtHost = new JTextField();
    private JLabel lblPort = new JLabel("Port: ");
    private JTextField txtPort = new JTextField();
    private JButton btnConnect = new JButton("Connect");

    /****** Dialog Panel ******/
    private JTextArea dialogArea = new JTextArea();
    private JButton btnReady = new JButton("Ready");
    private JButton btnNotReady = new JButton("Not Ready");
    private JButton btnSurrender = new JButton("Surrender");

    /****** Chat Panel ******/
    private JPanel chatPanel = new JPanel();
    private JTextField txtChat = new JTextField();
    private JButton btnSend = new JButton("Send");

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
        panelStatus.add(scorePanel);
        panelStatus.add(connectionPanel);
        panelStatus.add(buttonPanel);
        panelStatus.add(dialogPanel);
        panelStatus.add(chatPanel);

        /****** Score Panel ******/
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        Dimension dScore = new Dimension(200, 75);
        Dimension dBlack = new Dimension(200, 25);
        Dimension dWhite = new Dimension(200, 25);
        Dimension dButton = new Dimension(200, 25);
        scorePanel.setPreferredSize(dScore);
        JPanel playerBlackPanel = new JPanel();
        playerBlackPanel.setPreferredSize(dBlack);
        JPanel playerWhitePanel = new JPanel();
        playerWhitePanel.setPreferredSize(dWhite);
        buttonPanel.setPreferredSize(dButton);
        scorePanel.add(playerBlackPanel);
        scorePanel.add(playerWhitePanel);
        scorePanel.add(buttonPanel);
        playerBlackPanel.add(lbBlack);
        playerBlackPanel.add(countBlack);
        playerWhitePanel.add(lbWhite);
        playerWhitePanel.add(countWhite);
        buttonPanel.add(btnReady);
        buttonPanel.add(btnNotReady);
        buttonPanel.add(btnSurrender);
        btnNotReady.setVisible(false);
        btnSurrender.setEnabled(false);
        scorePanel.setVisible(false);

        /****** Connection Panel ******/
        connectionPanel.setLayout(new BoxLayout(connectionPanel, BoxLayout.Y_AXIS));
        hostPanel.setLayout(new BoxLayout(hostPanel, BoxLayout.X_AXIS));
        portPanel.setLayout(new BoxLayout(portPanel, BoxLayout.X_AXIS));
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
        Dimension dContainer = new Dimension(200, 75);
        Dimension dPanel = new Dimension(200, 25);
        Dimension dLbl = new Dimension(40, 25);
        Dimension dTxt = new Dimension(160, 25);
        Dimension dBtn = new Dimension(100, 25);
        connectionPanel.setPreferredSize(dContainer);
        hostPanel.setPreferredSize(dPanel);
        portPanel.setPreferredSize(dPanel);
        btnPanel.setPreferredSize(dPanel);
        lblHost.setPreferredSize(dLbl);
        lblPort.setPreferredSize(dLbl);
        txtHost.setPreferredSize(dTxt);
        txtPort.setPreferredSize(dTxt);
        btnConnect.setPreferredSize(dBtn);
        connectionPanel.add(hostPanel);
        connectionPanel.add(portPanel);
        connectionPanel.add(btnPanel);
        hostPanel.add(lblHost);
        hostPanel.add(txtHost);
        portPanel.add(lblPort);
        portPanel.add(txtPort);
        btnPanel.add(btnConnect);

        /****** Dialog Panel ******/
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
        Dimension dScroll = new Dimension(200, 425);
        JScrollPane dialogScroll = new JScrollPane(dialogArea);
        dialogScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        dialogScroll.setPreferredSize(dScroll);
        dialogPanel.add(dialogScroll);
        dialogArea.setEditable(false);

        /****** Chat Panel ******/
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.X_AXIS));
        Dimension dChat = new Dimension(200, 25);
        chatPanel.setPreferredSize(dChat);
        chatPanel.add(txtChat);
        chatPanel.add(btnSend);
        txtChat.setEnabled(false);
        btnSend.setEnabled(false);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    sendMessage("quit", "yes"); //send quit command to server
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
    }

    //main method access
    public static void main(String[] args) {
        MainFrame mf = new MainFrame();
        mf.login();

        JMenuBar menuBar = new JMenuBar();

        // File Menu, F - Mnemonic
        JMenu fileMenu = new JMenu("Start");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        // File->New, N - Mnemonic
        JMenuItem newMenuItem = new JMenuItem("Help", KeyEvent.VK_N);
        fileMenu.add(newMenuItem);

        mf.setJMenuBar(menuBar);
    }

    private void login(){
        btnConnect.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                host = txtHost.getText();
                if(txtHost.getText().equals(""))
                    JOptionPane.showMessageDialog(connectionPanel, "Please enter host address!");
                else if (txtPort.getText().equals(""))
                    JOptionPane.showMessageDialog(connectionPanel, "Please enter port!");
                else{
                    try {
                        port = Integer.parseInt(txtPort.getText());
                        if(connection()){   //if connection is successful
                            connectionPanel.setVisible(false);
                            scorePanel.setVisible(true);
                            launch();
                            getMessage();
                        }
                    }catch (NumberFormatException e1){
                        JOptionPane.showMessageDialog(connectionPanel, "Port has to be a number!");
                    }
                }
            }
        });
    }

    private boolean connection(){
        try {
            socket = new Socket(host, port);
            connected = socket.isConnected();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(connectionPanel, "You are entering wrong host or port number");
        }
        return connected;
    }

    private void launch(){
        try {
            btnReady.addMouseListener(new MouseAdapter() {  //ready button listener
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    sendMessage("command", "ready");
                }
            });
            btnNotReady.addMouseListener(new MouseAdapter() {   //not ready button listener
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    sendMessage("command", "notReady");
                    btnNotReady.setVisible(false);
                    btnReady.setVisible(true);
                }
            });
            btnSurrender.addMouseListener(new MouseAdapter() {  //surrender button listener
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    int confirm;
                    if (inGame) {
                        confirm = JOptionPane.showConfirmDialog(null, "Are you sure to surrender?\nYour rival will win this game automatically!", "No", JOptionPane.YES_NO_OPTION);
                        if (confirm == 0) sendMessage("command", "surrender");
                    }
                }
            });
            txtChat.addActionListener(e -> {    //chat field listener
                sendChatMessage(txtChat.getText());
                txtChat.setText("");
            });
            btnSend.addActionListener(e -> {    //send button listener
                sendChatMessage(txtChat.getText());
                txtChat.setText("");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * send chat message to server
     * @param msg: message content
     */
    private void sendChatMessage(String msg){
        if(!(msg).equals(""))
            sendMessage("chat", msg);
    }

    /**
     * get messages from server
     */
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

    /**
     * send message to server
     * @param key: json key
     * @param value: json value
     */
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

    /**
     * command get from server
     * @param cmd: command content
     */
    private void Command(String cmd) {
        JSONObject jsonGet = new JSONObject(cmd);
        try {
            if (cmd.contains("\"show\":")) {    //refresh game map
                String mapString = jsonGet.get("show").toString();
                refreshMap(mapString);
            } else if (cmd.contains("\"message\":")) {  //print message in message area
                dialogArea.append(jsonGet.get("message").toString() + "\n");
                dialogArea.setCaretPosition(dialogArea.getText().length()); //keep the last line on the bottom
            } else if (cmd.contains("\"score\":")) {    //show score in the corner
                countBlack.setText(jsonGet.get("score").toString().replace("[", "").replace("]", "").split(",")[0]);
                countWhite.setText(jsonGet.get("score").toString().replace("[", "").replace("]", "").split(",")[1]);
            } else if(cmd.contains("\"game\":")){   //game status change command from server
                if(jsonGet.get("game").toString().equals("on")) {   //player in game
                    btnReady.setVisible(false);
                    btnNotReady.setVisible(false);
                    btnSurrender.setEnabled(true);
                    txtChat.setEnabled(true);
                    btnSend.setEnabled(true);
                    inGame = true;
                } else if(jsonGet.get("game").toString().equals("off")) {   //player out of game
                    inGame = false;
                    btnReady.setVisible(true);
                    btnNotReady.setVisible(false);
                    btnSurrender.setEnabled(false);
                    txtChat.setEnabled(false);
                    btnSend.setEnabled(false);
                } else if(jsonGet.get("game").toString().equals("ready")) { //player is allowed to be ready
                    inGame = false;
                    btnReady.setVisible(false);
                    btnNotReady.setVisible(true);
                    btnSurrender.setEnabled(false);
                    txtChat.setEnabled(false);
                    btnSend.setEnabled(false);
                } else if(jsonGet.get("game").toString().equals("wait")) {  //player is not allowed to be ready
                    inGame = false;
                    btnReady.setVisible(true);
                    btnNotReady.setVisible(false);
                    btnSurrender.setEnabled(false);
                    txtChat.setEnabled(false);
                    btnSend.setEnabled(false);
                }
            } else if(cmd.contains("\"current\":")){    //get the current player color from server
                if (jsonGet.get("current").toString().equals("1"))
                    currentPlayer = 1;
                else if (jsonGet.get("current").toString().equals("-1"))
                    currentPlayer = -1;
            } else if(cmd.contains("\"warning\":")){    //show warning message
                JOptionPane.showMessageDialog(null, jsonGet.get("warning").toString());
            }
        }catch (Exception e){
            System.out.println("Unknown command");
        }
    }

    /**
     * refresh game map
     * @param jsonString: map json value
     * @return current map
     */
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

    //game map panel
    private class GamePanel extends JPanel implements MouseListener{
        int [][] piece = new int [8][8];
        GamePanel(int[][] piece){
            this.piece = piece;
            Dimension dMap = new Dimension(500, 500);
            addMouseListener(this);
            this.setBackground(BACKGROUND_COLOR);
            this.setPreferredSize(dMap);
        }

        /**
         * draw current map
         * @param g: pencil
         */
        private void drawMap(Graphics g) {
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