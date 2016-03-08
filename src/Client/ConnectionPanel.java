//package Client;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.*;
//import java.io.*;
//import java.net.*;
//import java.util.Arrays;
//
//class ConnectionPanel extends JPanel {
//    private String host;
//    private int port;
//    private Socket socket = null;
//    private JSONPost json = new JSONPost();
////    private MessageBoy messageBoy = new MessageBoy();
//
//    /***** components ******/
//    private JPanel hostPanel = new JPanel();
//    private JPanel portPanel = new JPanel();
//    private JPanel btnPanel = new JPanel();
//    private JLabel lblHost = new JLabel("Host: ");
//    private JTextField txtHost = new JTextField();
//    private JLabel lblPort = new JLabel("Port: ");
//    private JTextField txtPort = new JTextField();
//    private JButton btnConnect = new JButton("Connect");
//    private JButton btnDisconnect = new JButton("Disconnect");
//    private JButton btnReady = new JButton("Ready");
//    private JButton btnNotReady = new JButton("Not Ready");
//
//    ConnectionPanel() {
//        /****** layout ******/
//        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//        hostPanel.setLayout(new BoxLayout(hostPanel, BoxLayout.X_AXIS));
//        portPanel.setLayout(new BoxLayout(portPanel, BoxLayout.X_AXIS));
//        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
//
//        /****** dimension ******/
//        Dimension dContainer = new Dimension(200, 75);
//        Dimension dPanel = new Dimension(200, 25);
//        Dimension dLbl = new Dimension(40, 25);
//        Dimension dTxt = new Dimension(160, 25);
//        Dimension dBtn = new Dimension(100, 25);
//        this.setPreferredSize(dContainer);
//        hostPanel.setPreferredSize(dPanel);
//        portPanel.setPreferredSize(dPanel);
//        btnPanel.setPreferredSize(dPanel);
//        lblHost.setPreferredSize(dLbl);
//        lblPort.setPreferredSize(dLbl);
//        txtHost.setPreferredSize(dTxt);
//        txtPort.setPreferredSize(dTxt);
//        btnConnect.setPreferredSize(dBtn);
//        btnDisconnect.setPreferredSize(dBtn);
//        btnReady.setPreferredSize(dBtn);
//        btnNotReady.setPreferredSize(dBtn);
//
//        /****** add ******/
//        this.add(hostPanel);
//        this.add(portPanel);
//        this.add(btnPanel);
//        hostPanel.add(lblHost);
//        hostPanel.add(txtHost);
//        portPanel.add(lblPort);
//        portPanel.add(txtPort);
//        btnPanel.add(btnConnect);
//        btnPanel.add(btnDisconnect);
//        btnPanel.add(btnReady);
//        btnPanel.add(btnNotReady);
//        btnReady.setVisible(false);
//        btnNotReady.setVisible(false);
//        btnDisconnect.setVisible(false);
//
//        initConnect();
//    }
//
//    private void initConnect() {
//        new Thread(){
//            @Override
//            public void run(){
//                btnConnect.addMouseListener(new MouseAdapter() {
//                    @Override
//                    public void mouseClicked(MouseEvent e) {
//                        btnConnect.setVisible(false);
//                        btnDisconnect.setVisible(true);
//                        connect();
//                    }
//                });
//
//                btnDisconnect.addMouseListener(new MouseAdapter() {
//                    @Override
//                    public void mouseClicked(MouseEvent e) {
//                        if(socket != null)
//                            try {
//                                socket.close();
//                            } catch (IOException e1) {
//                                e1.printStackTrace();
//                            }
//                        btnDisconnect.setVisible(false);
//                        btnReady.setVisible(false);
//                        btnNotReady.setVisible(false);
//                        btnConnect.setVisible(true);
//                    }
//                });
//            }
//        }.start();
//    }
//
//    private void connect(){
//        if(txtHost != null && txtPort != null) {
//            host = txtHost.getText();
//            port = Integer.parseInt(txtPort.getText());
//            try {
//                socket = new Socket(host, port);
//                ready();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        else
//            System.out.println("Aren't you forget something?");
//    }
//
//    private void ready(){
//        btnReady.setVisible(true);
//        btnReady.addMouseListener(new MouseAdapter(){
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                super.mouseClicked(e);
//                hostPanel.setVisible(false);
//                portPanel.setVisible(false);
//                btnReady.setVisible(false);
//                btnNotReady.setVisible(true);
//                //new ObserverPool(socket);
////                 messageBoy.sendMessage(socket, "command", "ready");
////                getInitialMap();
//            }
//        });
//
//        btnNotReady.addMouseListener(new MouseAdapter(){
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                btnNotReady.setVisible(false);
//                btnReady.setVisible(true);
//            }
//        });
//
//    }
//
//    private void getInitialMap(){
//        new Thread() {
//            @Override
//            public void run() {
////                IGet getter = new Beacon();
//                String result = json.get(socket, "show").toString();
//                String [] stringArray = result.replace("[", "").replace("]", "").split(",");
//                int[][] map = new int[8][8];
//                for(int y=0; y<8; y++){
//                    for(int x=0; x<8; x++){
//                        map[x][y] = Integer.parseInt(stringArray[x * y]);
//                    }
//                }
//                System.out.println(Arrays.deepToString(map));
//            }
//        }.start();
//    }
//
//}
