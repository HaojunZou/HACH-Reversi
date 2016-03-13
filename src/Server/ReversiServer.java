package Server;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ReversiServer extends JFrame{
    private static LinkedList<Player> waitQueue = new LinkedList<Player>();	//list of all players are waiting
    private static LinkedList<Player> gameQueue = new LinkedList<Player>();	//list of all players are engaging
    private Algorithm algorithm = new Algorithm();

    private JLabel waitNumber = new JLabel(Integer.toString(waitQueue.size())); //number of online user
    private JLabel inGameNumber = new JLabel(Integer.toString(gameQueue.size()));   //number of in game user
    private JTextArea logContent = new JTextArea(); //server log

    private ReversiServer(int port){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            this.setTitle("HC-Reversi Server");
            this.setSize(500, 500);
            this.setVisible(true);

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
            logContent.append("Server on...\n");
            logContent.setCaretPosition(logContent.getText().length());

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
            for(;;){
                Socket client = serverSocket.accept();
                Player player = new Player();
                player.setSocket(client);
                waitQueue.add(player);
                waitNumber.setText(Integer.toString(waitQueue.size()));
                logContent.append("Client connected:" + client.getRemoteSocketAddress().toString() + "\n");
                logContent.setCaretPosition(logContent.getText().length());
                Thread t = new Thread(new ClientThread(player));    //build a new thread to client
                t.start();
            }
        }catch(IOException e){
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
            //TODO: login function, return a player socket, user "player.setSocket = socket"  to set current player socket
            getMessage();
        }

        @Override
        public void getMessage(){
            try {
                player.getSocket().setSoTimeout(1200000);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(player.getSocket().getInputStream(), "UTF-8"));
                sendMessage(player.getSocket(), "message", "--- Welcome to HC-Reversi ---", "me");
                String command;
                while ((command = bufferedReader.readLine()) != null) {
                    JSONObject jsonGet = new JSONObject(command);
                    if(command.contains("\"quit\":")){
                        if(jsonGet.get("quit").equals("yes")){
                            if(player.isInGame()){
                                gameQueue.stream().filter(p -> p.getSocket() != player.getSocket()).forEach(p -> {
                                    sendMessage(p.getSocket(), "message", "Your opponent left the game", "me");
                                    gameOver = true;
                                });
                            }else
                                break;
                            player.getSocket().shutdownInput();
                            waitQueue.remove(player);
                            waitNumber.setText(Integer.toString(waitQueue.size()));
                            inGameNumber.setText(Integer.toString(gameQueue.size()));
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
                    player.getSocket().shutdownInput();
                    player.getSocket().close();
                    waitQueue.remove(player);
                    waitNumber.setText(Integer.toString(waitQueue.size()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            logContent.append("Client " + player.getSocket().toString() + " Disconnected\n");
            logContent.setCaretPosition(logContent.getText().length());
        }

        @Override
        public void sendMessage(Socket socket, String key, Object value, String who){
            BufferedWriter bufferedWriter;
            try {
                if(who.equals("all")) {
                    for (Player p : gameQueue) {
                        if (p.getSocket().isConnected()) {
                            JSONObject jsonSend = new JSONObject();
                            jsonSend.put(key, value);
                            bufferedWriter = new BufferedWriter(new OutputStreamWriter(p.getSocket().getOutputStream(), "UTF-8"));
                            String jsonString = jsonSend.toString();
                            bufferedWriter.write(jsonString);
                            bufferedWriter.write("\r\n");
                            bufferedWriter.flush();
                        }
                    }
                }
                else if(who.equals("me")){
                    if (socket.isConnected()) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(key, value);
                        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                        String jsonString = jsonObject.toString();
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
                            sendMessage(player.getSocket(), "wait", "yes", "me");
                            sendMessage(player.getSocket(), "warning", "There's a game running, please wait...", "me");
                        }
                        else {
                            gameQueue.add(player);
                            waitQueue.remove(player);
                            if(gameQueue.size() == 2)  //if game queue has two players, start a game
                                gameStart();
                        }
                        waitNumber.setText(Integer.toString(waitQueue.size()));
                        inGameNumber.setText(Integer.toString(gameQueue.size()));
                    } else if(jsonGet.get("command").equals("notReady")){
                        waitQueue.add(player);
                        gameQueue.remove(player);
                        waitNumber.setText(Integer.toString(waitQueue.size()));
                        inGameNumber.setText(Integer.toString(gameQueue.size()));
                    } else if(jsonGet.get("command").toString().equals("surrender")){
                        sendMessage(player.getSocket(), "game", "off", "all");
                        //no matter who has more piece in the map, surrender will affect the opponent wins
                        sendMessage(player.getSocket(), "message", (player.getColor()==1) ? "White wins" : "Black wins", "all");
                        waitQueue.add(gameQueue.getFirst());
                        waitQueue.add(gameQueue.getLast());
                        gameQueue.clear();
                        waitNumber.setText(Integer.toString(waitQueue.size()));
                        inGameNumber.setText(Integer.toString(gameQueue.size()));
                        algorithm = new Algorithm();
                    }
                } else if (cmd.contains("\"move\":")) {
                    if (player.getColor() == algorithm.getCurrentPlayer()) {
                        int x = Integer.parseInt(jsonGet.get("move").toString().replace("[", "").replace("]", "").split(",")[0]);
                        int y = Integer.parseInt(jsonGet.get("move").toString().replace("[", "").replace("]", "").split(",")[1]);
                        //if(algorithm.checkLegal()) {
                        sendMessage(player.getSocket(), "message",
                                (algorithm.getCurrentPlayer() == 1) ?
                                        ("Black " + "[" + getX(x) + "," + (y + 1) + "]")
                                        : ("White " + "[" + getX(x) + "," + (y + 1) + "]"),
                                "all");
                        //}
                        if (algorithm.move(x, y) == 2) {   //switch player
                            sendMessage(player.getSocket(), "current", algorithm.getCurrentPlayer(), "all");
                            sendMessage(player.getSocket(), "show", algorithm.getCurrentMap(), "all");
                            sendMessage(player.getSocket(), "score", getScore(), "all");
                        } else if (algorithm.move(x, y) == 1) {  //opponent player should pass
                            sendMessage(player.getSocket(), "current", algorithm.getCurrentPlayer(), "all");
                            sendMessage(player.getSocket(), "show", algorithm.getCurrentMap(), "all");
                            sendMessage(player.getSocket(), "score", getScore(), "all");
                            sendMessage(player.getSocket(), "message", "Your rival pass, go on", "me");
                            Player passPlayer = null;
                            for (Player p : gameQueue) {
                                if (p != player)
                                    passPlayer = p;
                            }
                            assert passPlayer != null;
                            sendMessage(passPlayer.getSocket(), "message", "Pass", "me");
                        } else if (algorithm.move(x, y) == 0) {   //if no player can move, game over
                            sendMessage(player.getSocket(), "message", "Game over!", "all");
                            sendMessage(player.getSocket(), "show", algorithm.getCurrentMap(), "all");
                            sendMessage(player.getSocket(), "score", getScore(), "all");
                            if ((getScore()[0]) > (getScore()[1]))
                                sendMessage(player.getSocket(), "message", "Black wins!", "all");
                            else if ((getScore()[0]) < (getScore()[1]))
                                sendMessage(player.getSocket(), "message", "White wins!", "all");
                            else
                                sendMessage(player.getSocket(), "message", "Draw!", "all");
                            sendMessage(player.getSocket(), "game", "off", "all");
                            sendMessage(player.getSocket(), "show", algorithm.getCurrentMap(), "all");
                            gameOver = true;
                        }
                    }
                }
            }catch (Exception e) {
                logContent.append("Unknown command\n");
                logContent.setCaretPosition(logContent.getText().length());
            }
        }

        private void gameStart(){
            gameQueue.getFirst().setColor(1);   //black player
            gameQueue.getFirst().setInGame(true);   //set player status
            sendMessage(gameQueue.getFirst().getSocket(), "message", "Game started!\nYou play as black", "me");
            gameQueue.getLast().setColor(-1);   //white player
            gameQueue.getLast().setInGame(true);
            sendMessage(gameQueue.getLast().getSocket(), "message", "Game started!\nYou play as white", "me");
            sendMessage(player.getSocket(), "game", "on", "all");   //tell client change status
            sendMessage(player.getSocket(), "current", algorithm.getCurrentPlayer(), "all");    //send current player move
            sendMessage(player.getSocket(), "show", algorithm.getCurrentMap(), "all");  //send map
            sendMessage(player.getSocket(), "score", getScore(), "all");    //send current score
            waitNumber.setText(Integer.toString(waitQueue.size()));
            inGameNumber.setText(Integer.toString(gameQueue.size()));
            new Thread(new Game()).start(); //new thread for a game
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
                        else if(gameOver)
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    for(Player p : gameQueue){
                        p.setInGame(false);
                        waitQueue.add(p);
                        sendMessage(p.getSocket(), "game", "off", "me");
                    }
                    gameQueue.clear();
                    waitNumber.setText(Integer.toString(waitQueue.size()));
                    inGameNumber.setText(Integer.toString(gameQueue.size()));
                    algorithm = new Algorithm();
                    gameOver = false;
                }
            }
        }   //end of Game thread
    }   //end of Client thread
}