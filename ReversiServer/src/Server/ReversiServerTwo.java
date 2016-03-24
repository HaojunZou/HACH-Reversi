package Server;

/**
 * This server take only two players at same time
 */

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ReversiServerTwo extends JFrame{
    private static LinkedList<Player> onlineQueue = new LinkedList<Player>();	//list of all players are waiting
    private static LinkedList<Player> table = new LinkedList<Player>();	//list of all players are engaging
    private Algorithm algorithm = new Algorithm();

    private JLabel onlineNumber = new JLabel(Integer.toString(onlineQueue.size())); //number of online user
    private JLabel inGameNumber = new JLabel(Integer.toString(table.size()));   //number of in game user
    private JTextArea logContent = new JTextArea(); //server log

    private ReversiServerTwo(int port){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            this.setTitle("HACH-Reversi Server");
            this.setSize(500, 500);
            this.setVisible(true);
            this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            JPanel mainPanel = new JPanel();
            JPanel serverInfo = new JPanel();   //server information
            JLabel lbHost = new JLabel("Host: " + serverSocket.getLocalSocketAddress().toString());
            JPanel onlinePanel = new JPanel();
            JPanel inGamePanel = new JPanel();
            JLabel lbOnline = new JLabel("Online users: ");
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
            onlinePanel.add(onlineNumber);
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
            new ReversiServerTwo(Integer.parseInt(PORT));
        }
    }

    //client thread
    private class ClientThread implements Runnable, MessageBoy{
        Player player;
        boolean gameOver = false;

        ClientThread(Player p){
            this.player = p;
        }

        @Override
        public void run(){
            getMessage();
            printLog("Client Disconnected: " + player.getSocket().getRemoteSocketAddress().toString());
        }

        @Override
        public void getMessage(){
            BufferedReader bufferedReader;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(player.getSocket().getInputStream(), "UTF-8"));
                sendMessage(player, "message", "--- Welcome to HACH-Reversi ---\n>>> Click [Ready] button");
                String command;
                while ((command = bufferedReader.readLine()) != null) {
                    JSONObject jsonGet = new JSONObject(command);
                    if(command.startsWith("{\"quit\":")){  //if get quit command from client, tell the other player
                        if(jsonGet.get("quit").equals("yes")){
                            if(player.isInGame()) {
                                table.stream().filter(p -> p.getSocket() != player.getSocket()).forEach(p -> {
                                    sendMessage(p, "message", ">>> Your rival left the game!");
                                });
                                gameUpdate(table);
                                gameEnd(table); //game should not keep running after that
                                serverUpdate();
                                break;
                            }
                        }
                    } else{
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
        }

        /**
         * send message to current player
         * @param player: current player
         * @param key: json key
         * @param value: json value
         */
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

        /**
         * send message to both player
         * @param players: players in game
         * @param key: json key
         * @param value: json value
         */
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

        /**
         * get command from client
         * @param cmd: command content
         */
        private void Command(String cmd) {
            JSONObject jsonGet = new JSONObject(cmd);
            if (cmd.startsWith("{\"command\":")) {
                if(jsonGet.get("command").equals("ready")){
                    if(table.size() >= 2){   //if table has 2 player, tell the new player to wait
                        sendMessage(player, "game", "wait");
                        sendMessage(player, "warning", "There's a game running, please try ready later...");
                    }
                    else {
                        table.add(player);  //add player to table
                        if(table.size() < 2)    //if there's only one player, send allow ready
                            sendMessage(player, "game", "ready");
                        else if (table.size() == 2)  //if table has two players, start a game
                            gameStart(table);
                    }
                    serverUpdate();
                } else if(jsonGet.get("command").equals("notReady")){   //if player don't want to be ready
                    table.remove(player);
                    serverUpdate();
                } else if(jsonGet.get("command").toString().equals("surrender")){   //if player want surrender
                    //no matter who has more pieces in the map, surrender will affect the opponent wins
                    table.stream().filter(p -> p.getSocket() != player.getSocket()).forEach(p -> {
                        sendMessage(p, "message", ">>> Your rival surrendered!");
                    });
                    sendAllMessage(table, "message", (player.getColor()==1) ? ">>> White wins!" : ">>> Black wins!");
                    gameUpdate(table);
                    gameEnd(table);
                }
            } else if (cmd.startsWith("{\"move\":")) { //player put a piece in the map
                if (player.getColor() == algorithm.getCurrentPlayer()) {    //if it's the current player move
                    int x = Integer.parseInt(jsonGet.get("move").toString().replace("[", "").replace("]", "").split(",")[0]);
                    int y = Integer.parseInt(jsonGet.get("move").toString().replace("[", "").replace("]", "").split(",")[1]);
                    int nextPlayer = algorithm.move(x, y);
                    if (nextPlayer == -player.getColor() && nextPlayer != 0) {   //switch player
                        //result shows player color before switch
                        sendAllMessage(table, "message",
                                (algorithm.getCurrentPlayer() == -1) ?
                                        ("Black " + "[" + getX(x) + "," + (y + 1) + "]")
                                        : ("White " + "[" + getX(x) + "," + (y + 1) + "]"));
                        gameUpdate(table);
                    } else if (nextPlayer == player.getColor() && nextPlayer != 0) {  //if not switch player
                        //result shows same color before move
                        sendAllMessage(table, "message",
                                (algorithm.getCurrentPlayer() == 1) ?
                                        ("Black " + "[" + getX(x) + "," + (y + 1) + "]")
                                        : ("White " + "[" + getX(x) + "," + (y + 1) + "]"));
                        sendMessage(player, "message", ">>> Your rival pass, go on!");
                        Player passPlayer = null;
                        for (Player p : table) {
                            if (p != player)
                                passPlayer = p;
                        }
                        assert passPlayer != null;
                        gameUpdate(table);
                        sendMessage(passPlayer, "message", ">>> Pass!");
                    } else if(nextPlayer == 64 || nextPlayer == -64){   //if no player can move, game over
                        sendAllMessage(table, "message",
                                ((algorithm.getCurrentPlayer()/64) == 1) ?
                                        ("Black " + "[" + getX(x) + "," + (y + 1) + "]")
                                        : ("White " + "[" + getX(x) + "," + (y + 1) + "]"));
                        gameUpdate(table);
                    }
                }
            } else if(cmd.startsWith("{\"chat\":")){   //get chat message
                sendAllMessage(table, "message", ((player.getColor() == 1) ? "Black say: " : "White say: ") + jsonGet.get("chat"));
            }
        }

        /**
         * initialize game
         * @param players: two ready players
         */
        private void gameStart(LinkedList<Player> players){
            gameOver = false;
            printLog("One game started");
            players.getFirst().setColor(1);   //black player
            players.getFirst().setInGame(true);   //set player status
            players.getLast().setColor(-1);   //white player
            players.getLast().setInGame(true);
            sendAllMessage(players, "message", "---------------------------------------------");
            sendMessage(players.getFirst(), "message", ">>> Rival found!\n>>> Game started!\n>>> You play as black");
            sendMessage(players.getLast(), "message", ">>> Rival found!\n>>> Game started!\n>>> You play as white");
            sendAllMessage(players, "game", "on");   //tell client change status
            gameUpdate(players);
            serverUpdate();
            new Thread(new Game()).start(); //new thread for a game
        }

        /**
         * game end
         * @param players: two in game players
         */
        private void gameEnd(LinkedList<Player> players){
            for(Player p : players){
                p.setInGame(false);
                sendMessage(p, "game", "off");
                sendMessage(p, "message", ">>> Game Over!");
            }
            table.clear();  //empty the table
            serverUpdate();
            printLog("One game ended");
            algorithm = new Algorithm();    //restore the algorithm
        }

        /**
         * during the game
         * @param players: two in game players
         */
        private void gameUpdate(LinkedList<Player> players){
            sendAllMessage(players, "current", algorithm.getCurrentPlayer());    //send current player move
            sendAllMessage(players, "score", getScore());    //send current score
            Player curP = null; //the current player
            Player rivP = null; //the rival player
            for(Player p : players){    //send map
                if(p.getColor() == algorithm.getCurrentPlayer())
                    curP = p;
                else if(p.getColor() == -algorithm.getCurrentPlayer())
                    rivP = p;
                else {
                    sendAllMessage(players, "show", algorithm.getCurrentMapWithoutAva());   //show map just before game over
                    if ((getScore()[0]) > (getScore()[1]))
                        sendAllMessage(players, "message", ">>> Black wins!");
                    else if ((getScore()[0]) < (getScore()[1]))
                        sendAllMessage(players, "message", ">>> White wins!");
                    else
                        sendAllMessage(players, "message", ">>> Draw!");
                    gameEnd(players);
                }
            }
            sendMessage(curP, "show", algorithm.getCurrentMap());   //show map with hint position
            sendMessage(rivP, "show", algorithm.getCurrentMapWithoutAva()); //show map without hint position
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
                        Command(command);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    for(Player p : table){
                        p.setInGame(false); //restore players status to not in game
                        gameUpdate(table);
                    }
                    sendAllMessage(table, "game", "off");
                    gameEnd(table);
                    serverUpdate();
                    table.clear();  //remove all players from table
                }
            }
        }   //end of Game thread
    }   //end of Client thread

    private void serverUpdate(){
        onlineNumber.setText(Integer.toString(onlineQueue.size()));
        inGameNumber.setText(Integer.toString(table.size()));
    }

    private void printLog(String log){
        logContent.append(log + "\n");
        logContent.setCaretPosition(logContent.getText().length());
    }
}