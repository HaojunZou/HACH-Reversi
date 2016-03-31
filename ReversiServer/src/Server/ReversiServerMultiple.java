package Server;

/**
 * This server take more than two players at same time
 */

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ReversiServerMultiple{
    private static LinkedList<Player> onlineQueue = new LinkedList<Player>();	//list of all players are online
    private static LinkedList<Player> waitingQueue = new LinkedList<Player>();	//list of all players are waiting
    private static LinkedList<LinkedList<Player>> tables = new LinkedList<LinkedList<Player>>();    //list of all active tables
    private static LinkedList<Algorithm> algorithms = new LinkedList<Algorithm>();

    private ReversiServerMultiple(int port){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Server on...");
            for(;;) {
                Socket client = serverSocket.accept();
                Player player = new Player();
                player.setSocket(client);
                onlineQueue.add(player);
                serverUpdate();
                System.out.println("Client connected:" + client.getRemoteSocketAddress().toString());
                Thread t = new Thread(new ClientThread(player));    //build a new thread to client
                t.start();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //main access
    public static void main(String [] args){
        new ReversiServerMultiple(20000);
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
            System.out.println("Client Disconnected: " + player.getSocket().getRemoteSocketAddress().toString());
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
                                tables.get(player.getTableID()).stream().filter(p -> p.getSocket() != player.getSocket()).forEach(p -> sendMessage(p, "message", ">>> Your rival left the game!"));
                                gameUpdate(tables.get(player.getTableID()));
                                gameEnd(tables.get(player.getTableID())); //game should not keep running after that
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
                    if(waitingQueue.size() >= 2){   //if waitingQueue has 2 player, tell the new player to wait
                        sendMessage(player, "game", "wait");
                        sendMessage(player, "warning", "There's a game running, please try ready later...");
                    }
                    else {
                        waitingQueue.add(player);  //add player to waitingQueue
                        if(waitingQueue.size() < 2)    //if there's only one player, send allow ready
                            sendMessage(player, "game", "ready");
                        else if (waitingQueue.size() == 2)  //if waitingQueue has two players, start a game
                            gameStart(waitingQueue);
                    }
                    serverUpdate();
                } else if(jsonGet.get("command").equals("notReady")){   //if player don't want to be ready
                    waitingQueue.remove(player);
                    serverUpdate();
                } else if(jsonGet.get("command").toString().equals("surrender")){   //if player want surrender
                    //no matter who has more pieces in the map, surrender will affect the opponent wins
                    tables.get(player.getTableID()).stream().filter(p -> p.getSocket() != player.getSocket()).forEach(p -> sendMessage(p, "message", ">>> Your rival surrendered!"));
                    sendAllMessage(tables.get(player.getTableID()), "message", (player.getColor()==1) ? ">>> White wins!" : ">>> Black wins!");
                    gameUpdate(tables.get(player.getTableID()));
                    gameEnd(tables.get(player.getTableID()));
                }
            } else if (cmd.startsWith("{\"move\":")) { //player put a piece in the map
                if (player.getColor() == algorithms.get(player.getAlgorithmID()).getCurrentPlayer()) {    //if it's the current player move
                    int x = Integer.parseInt(jsonGet.get("move").toString().replace("[", "").replace("]", "").split(",")[0]);
                    int y = Integer.parseInt(jsonGet.get("move").toString().replace("[", "").replace("]", "").split(",")[1]);
                    int nextPlayer = algorithms.get(player.getAlgorithmID()).move(x, y);
                    if (nextPlayer == -player.getColor() && nextPlayer != 0) {   //switch player
                        //result shows player color before switch
                        sendAllMessage(tables.get(player.getTableID()), "message",
                                (algorithms.get(player.getAlgorithmID()).getCurrentPlayer() == -1) ?
                                        ("Black " + "[" + getX(x) + "," + (y + 1) + "]")
                                        : ("White " + "[" + getX(x) + "," + (y + 1) + "]"));
                        gameUpdate(tables.get(player.getTableID()));
                    } else if (nextPlayer == player.getColor() && nextPlayer != 0) {  //if not switch player
                        //result shows same color before move
                        sendAllMessage(tables.get(player.getTableID()), "message",
                                (algorithms.get(player.getAlgorithmID()).getCurrentPlayer() == 1) ?
                                        ("Black " + "[" + getX(x) + "," + (y + 1) + "]")
                                        : ("White " + "[" + getX(x) + "," + (y + 1) + "]"));
                        sendMessage(player, "message", ">>> Your rival pass, go on!");
                        Player passPlayer = null;
                        for (Player p : tables.get(player.getTableID())) {
                            if (p != player)
                                passPlayer = p;
                        }
                        assert passPlayer != null;
                        gameUpdate(tables.get(player.getTableID()));
                        sendMessage(passPlayer, "message", ">>> Pass!");
                    } else if(nextPlayer == 64 || nextPlayer == -64){   //if no player can move, game over
                        sendAllMessage(tables.get(player.getTableID()), "message",
                                ((algorithms.get(player.getAlgorithmID()).getCurrentPlayer()/64) == 1) ?
                                        ("Black " + "[" + getX(x) + "," + (y + 1) + "]")
                                        : ("White " + "[" + getX(x) + "," + (y + 1) + "]"));
                        gameUpdate(tables.get(player.getTableID()));
                    }
                }
            } else if(cmd.startsWith("{\"chat\":")){   //get chat message
                sendAllMessage(tables.get(player.getTableID()), "message", ((player.getColor() == 1) ? "Black say: " : "White say: ") + jsonGet.get("chat"));
            }
        }

        /**
         * initialize game
         * @param players: two ready players
         */
        private void gameStart(LinkedList<Player> players){
            gameOver = false;
            Algorithm algorithm = new Algorithm();
            System.out.println("One game started");
            players.getFirst().setColor(1);   //black player
            players.getFirst().setInGame(true);   //set player status
            players.getFirst().setTableID(tables.size());
            players.getFirst().setAlgorithmID(tables.size());
            players.getLast().setColor(-1);   //white player
            players.getLast().setInGame(true);
            players.getLast().setTableID(tables.size());
            players.getLast().setAlgorithmID(tables.size());
            sendAllMessage(players, "message", "---------------------------------------------");
            sendMessage(players.getFirst(), "message", ">>> Rival found!\n>>> Game started!\n>>> You play as black");
            sendMessage(players.getLast(), "message", ">>> Rival found!\n>>> Game started!\n>>> You play as white");
            sendAllMessage(players, "game", "on");   //tell client change status
            tables.add(players);
            algorithms.add(algorithm);
            waitingQueue = new LinkedList<Player>();
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
            tables.set(players.getFirst().getTableID(), null);
            algorithms.set(players.getFirst().getAlgorithmID(), null);
            serverUpdate();
            System.out.println("One game ended");
            if(tables.size() == 0){
                tables.clear();
                algorithms.clear();
            }
        }

        /**
         * during the game
         * @param players: two in game players
         */
        private void gameUpdate(LinkedList<Player> players){
            sendAllMessage(players, "current", algorithms.get(players.getFirst().getAlgorithmID()).getCurrentPlayer());    //send current player move
            sendAllMessage(players, "score", getScore());    //send current score
            Player curP = null; //the current player
            Player rivP = null; //the rival player
            for(Player p : players){    //send map
                if(p.getColor() == algorithms.get(players.getFirst().getAlgorithmID()).getCurrentPlayer())
                    curP = p;
                else if(p.getColor() == -algorithms.get(players.getFirst().getAlgorithmID()).getCurrentPlayer())
                    rivP = p;
                else {
                    sendAllMessage(players, "show", algorithms.get(players.getFirst().getAlgorithmID()).getCurrentMapWithoutAva());   //show map just before game over
                    if ((getScore()[0]) > (getScore()[1]))
                        sendAllMessage(players, "message", ">>> Black wins!");
                    else if ((getScore()[0]) < (getScore()[1]))
                        sendAllMessage(players, "message", ">>> White wins!");
                    else
                        sendAllMessage(players, "message", ">>> Draw!");
                    gameEnd(players);
                }
            }
            sendMessage(curP, "show", algorithms.get(players.getFirst().getAlgorithmID()).getCurrentMap());   //show map with hint position
            sendMessage(rivP, "show", algorithms.get(players.getFirst().getAlgorithmID()).getCurrentMapWithoutAva()); //show map without hint position
        }

        private int[] getScore(){
            int[] score = new int[2];
            score[0] = algorithms.get(player.getAlgorithmID()).getCountBlack();
            score[1] = algorithms.get(player.getAlgorithmID()).getCountWhite();
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
                    gameUpdate(tables.get(player.getTableID()));
                    gameEnd(tables.get(player.getTableID()));
                }
            }
        }   //end of Game thread
    }   //end of Client thread

    private void serverUpdate(){
        int activeTables = 0;
        for(LinkedList t : tables){
            if(t != null)
                activeTables++;
        }
        System.out.println("Online users: " + Integer.toString(onlineQueue.size()));
        System.out.println("Tables: " + Integer.toString(activeTables));
    }

}