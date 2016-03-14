package Server;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ReversiServer extends JFrame{
    private static LinkedList<Player> onlineQueue = new LinkedList<Player>();	//list of all players are waiting
    private static LinkedList<Player> gameQueue = new LinkedList<Player>();	//list of all players are engaging
    private Algorithm algorithm = new Algorithm();

    private JLabel waitNumber = new JLabel(Integer.toString(onlineQueue.size())); //number of online user
    private JLabel inGameNumber = new JLabel(Integer.toString(gameQueue.size()));   //number of in game user
    private JTextArea logContent = new JTextArea(); //server log

    private ReversiServer(int port){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            this.setTitle("HC-Reversi Server");
            this.setSize(500, 500);
            this.setVisible(true);
            this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            JPanel mainPanel = new JPanel();
            JPanel serverInfo = new JPanel();   //server information
            JLabel lbHost = new JLabel("Host: " + serverSocket.getLocalSocketAddress().toString());
            JPanel onlinePanel = new JPanel();
            JPanel inGamePanel = new JPanel();
            JLabel lbOnline = new JLabel("Waiting users: ");
            JLabel lbInGame = new JLabel("In game users: ");
            JScrollPane logArea = new JScrollPane(logContent);

            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            serverInfo.setLayout(new BoxLayout(serverInfo, BoxLayout.Y_AXIS));
            onlinePanel.setLayout(new BoxLayout(onlinePanel, BoxLayout.X_AXIS));
            inGamePanel.setLayout(new BoxLayout(inGamePanel, BoxLayout.X_AXIS));
            Dimension dServerInfo = new Dimension(500, 50);
            Dimension dLogArea = new Dimension(500, 450);
            serverInfo.setPreferredSize(dServerInfo);
            logArea.setPreferredSize(dLogArea);
            logArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            this.add(mainPanel);
            mainPanel.add(serverInfo);
            mainPanel.add(logArea);
            serverInfo.add(lbHost);
            serverInfo.add(onlinePanel);
            serverInfo.add(inGamePanel);
            onlinePanel.add(lbOnline);
            onlinePanel.add(waitNumber);
            inGamePanel.add(lbInGame);
            inGamePanel.add(inGameNumber);
            logContent.setEditable(false);
            printLog("Server on...");

            this.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    try {
                        serverSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    System.exit(0);
                }
            });
            for(;;) {
                Socket client = serverSocket.accept();
                Player player = new Player();
                player.setSocket(client);
                onlineQueue.add(player);
                serverUpdate();
                printLog("Client connected:" + client.getRemoteSocketAddress().toString());
                Thread t = new Thread(new ClientThread(player));    //build a new thread to client
                t.start();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //main access
    public static void main(String [] args){
        if(args.length != 1) {
            System.out.println("Please control program arguments:\n[port]\n");
        }
        else {
            final String PORT = args[0];
            new ReversiServer(Integer.parseInt(PORT));
        }
    }

    private class ClientThread implements Runnable, MessageBoy{
        Player player;
        boolean gameOver = false;

        ClientThread(Player p){
            this.player = p;
        }

        @Override
        public void run(){
            getMessage();
        }

        @Override
        public void getMessage(){
            BufferedReader bufferedReader;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(player.getSocket().getInputStream(), "UTF-8"));
                sendMessage(player, "message", "--- Welcome to HACH-Reversi ---");
                String command;
                while ((command = bufferedReader.readLine()) != null) {
                    JSONObject jsonGet = new JSONObject(command);
                    if(command.contains("\"quit\":")){
                        if(jsonGet.get("quit").equals("yes")){
                            if(player.isInGame()){
                                gameQueue.stream().filter(p -> p.getSocket() != player.getSocket()).forEach(p -> {
                                    sendMessage(p, "message", "Your opponent left the game");
                                    gameEnd(gameQueue);
                                });
                            }else
                                break;
                            player.getSocket().shutdownInput();
                            serverUpdate();
                            break;
                        }
                    }else{
                        Command(command);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    player.getSocket().close();
                    onlineQueue.remove(player);
                    serverUpdate();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            printLog("Client " + player.getSocket().toString() + " Disconnected");
        }

        @Override
        public void sendMessage(Player player, String key, Object value){
            try {
                if (player.getSocket().isConnected()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(key, value);
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(player.getSocket().getOutputStream(), "UTF-8"));
                    String jsonString = jsonObject.toString();
                    bufferedWriter.write(jsonString);
                    bufferedWriter.write("\r\n");
                    bufferedWriter.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void sendAllMessage(LinkedList<Player> players, String key, Object value){
            try {
                for (Player p : players) {
                    if (p.getSocket().isConnected()) {
                        JSONObject jsonSend = new JSONObject();
                        jsonSend.put(key, value);
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(p.getSocket().getOutputStream(), "UTF-8"));
                        String jsonString = jsonSend.toString();
                        bufferedWriter.write(jsonString);
                        bufferedWriter.write("\r\n");
                        bufferedWriter.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void Command(String cmd) {
            JSONObject jsonGet = new JSONObject(cmd);
            try {
                if (cmd.contains("\"command\":")) {
                    if(jsonGet.get("command").equals("ready")){
                        if(gameQueue.size() >= 2){   //if game queue has 2 player, tell the new player to wait
                            sendMessage(player, "wait", "yes");
                            sendMessage(player, "warning", "There's a game running, please wait...");
                        }
                        else{
                            sendMessage(player, "wait", "no");
                            gameQueue.add(player);
                            if(gameQueue.size() == 2)  //if game queue has two players, start a game
                                gameStart(gameQueue);
                        }
                        serverUpdate();
                    } else if(jsonGet.get("command").equals("notReady")){
                        gameQueue.remove(player);
                        serverUpdate();
                    } else if(jsonGet.get("command").toString().equals("surrender")){
                        //no matter who has more pieces in the map, surrender will affect the opponent wins
                        sendAllMessage(gameQueue, "message", (player.getColor()==1) ? "White wins" : "Black wins");
                        gameEnd(gameQueue);
                        serverUpdate();
                    }
                } else if (cmd.contains("\"move\":")) {
                    if (player.getColor() == algorithm.getCurrentPlayer()) {
                        int x = Integer.parseInt(jsonGet.get("move").toString().replace("[", "").replace("]", "").split(",")[0]);
                        int y = Integer.parseInt(jsonGet.get("move").toString().replace("[", "").replace("]", "").split(",")[1]);
                        //if(algorithm.checkLegal()) {
                        sendAllMessage(gameQueue, "message",
                                (algorithm.getCurrentPlayer() == 1) ?
                                        ("Black " + "[" + getX(x) + "," + (y + 1) + "]")
                                        : ("White " + "[" + getX(x) + "," + (y + 1) + "]"));
                        //}
                        if (algorithm.move(x, y) == 2) {   //switch player
                            gameUpdate(gameQueue);
                        } else if (algorithm.move(x, y) == 1) {  //opponent player should pass
                            gameUpdate(gameQueue);
                            sendMessage(player, "message", "Your rival pass, go on");
                            Player passPlayer = null;
                            for (Player p : gameQueue) {
                                if (p != player)
                                    passPlayer = p;
                            }
                            assert passPlayer != null;
                            sendMessage(passPlayer, "message", "Pass");
                        } else if (algorithm.move(x, y) == 0) {   //if no player can move, game over
                            if ((getScore()[0]) > (getScore()[1]))
                                sendAllMessage(gameQueue, "message", "Black wins!");
                            else if ((getScore()[0]) < (getScore()[1]))
                                sendAllMessage(gameQueue, "message", "White wins!");
                            else
                                sendAllMessage(gameQueue, "message", "Draw!");
                            gameEnd(gameQueue);
                        }
                    }
                }
            }catch (Exception e) {
            }
        }

        private void gameStart(LinkedList<Player> players){
            gameOver = false;
            printLog("One game started");
            players.getFirst().setColor(1);   //black player
            players.getFirst().setInGame(true);   //set player status
            sendMessage(players.getFirst(), "message", "Game started!\nYou play as black");
            players.getLast().setColor(-1);   //white player
            players.getLast().setInGame(true);
            sendMessage(players.getLast(), "message", "Game started!\nYou play as white");
            sendAllMessage(players, "game", "on");   //tell client change status
            gameUpdate(players);
            serverUpdate();
            new Thread(new Game()).start(); //new thread for a game
        }

        private void gameEnd(LinkedList<Player> players){
            for(Player p : players){
                p.setInGame(false);
            }
            sendAllMessage(players, "game", "off");
            sendAllMessage(players, "message", "Game Over!");
            gameUpdate(players);
            gameQueue.clear();
            serverUpdate();
            printLog("One game ended");
            gameOver = true;
            algorithm = new Algorithm();
        }

        private void gameUpdate(LinkedList<Player> players){
            sendAllMessage(players, "current", algorithm.getCurrentPlayer());    //send current player move
            sendAllMessage(players, "show", algorithm.getCurrentMap());  //send map
            sendAllMessage(players, "score", getScore());    //send current score
        }

        private int[] getScore(){
            int[] score = new int[2];
            score[0] = algorithm.getCountBlack();
            score[1] = algorithm.getCountWhite();
            return score;
        }

        private char getX(int x){
            char letter = 0;
            switch (x){
                case 0: letter = 'A'; break;
                case 1: letter = 'B'; break;
                case 2: letter = 'C'; break;
                case 3: letter = 'D'; break;
                case 4: letter = 'E'; break;
                case 5: letter = 'F'; break;
                case 6: letter = 'G'; break;
                case 7: letter = 'H'; break;
                default: break;
            }
            return letter;
        }

        //Game thread
        class Game implements Runnable{
            Game(){
            }
            @Override
            public void run() {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(player.getSocket().getInputStream(), "UTF-8"));
                    String command;
                    while ((command = bufferedReader.readLine()) != null) {
                        if(!gameOver)
                            Command(command);
                        else if(gameOver) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    for(Player p : gameQueue){
                        p.setInGame(false);
                    }
                    sendAllMessage(gameQueue, "game", "off");
                    serverUpdate();
                    gameQueue.clear();
                }
            }
        }   //end of Game thread
    }   //end of Client thread

    private void serverUpdate(){
        waitNumber.setText(Integer.toString(onlineQueue.size()));
        inGameNumber.setText(Integer.toString(gameQueue.size()));
    }

    private void printLog(String log){
        logContent.append(log + "\n");
        logContent.setCaretPosition(logContent.getText().length());
    }
}