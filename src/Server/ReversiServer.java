package Server;

import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;

public class ReversiServer {
    private static LinkedList<Player> waitingQueue = new LinkedList<Player>();	//list of all players are waiting
    private static LinkedList<Player> gameQueue = new LinkedList<Player>();	//list of all players are engaging
    private Algorithm algorithm = new Algorithm();

    //constructor
    private ReversiServer(int port){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Reversi Server on...");
            for(;;){
                Socket client = serverSocket.accept();
                Player player = new Player();
                player.setSocket(client);
                waitingQueue.add(player);
                System.out.printf("Client connected: %s\n", client.getRemoteSocketAddress().toString());
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

    private class ClientThread implements Runnable, IReversi{
        Player player;

        ClientThread(Player p){
            this.player = p;
        }

        @Override
        public void run(){
            //TODO: login function, return a player socket, user "player.setSocket = socket"  to set current player socket
            sendMessage(player.getSocket(), "message", "--- Welcome to HC-Reversi ---", "me");
            if(player.getSocket().isConnected())
                getMessage();
        }

        class Game implements Runnable{
            Game(){
            }
            @Override
            public void run() {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(player.getSocket().getInputStream(), "UTF-8"));
                    String text;
                    while ((text = bufferedReader.readLine()) != null) {
                        JSONObject jsonGet = new JSONObject(text);
                        if (text.contains("\"move\":")) {
                            if (algorithm.getCurrentPlayer() == 64) {
                                //TODO: popup game over message
                            } else {
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
                                    algorithm.move(x, y);   //current player might change after this move
                                    sendMessage(player.getSocket(), "current", algorithm.getCurrentPlayer(), "all");
                                    sendMessage(player.getSocket(), "show", algorithm.getCurrentMap(), "all");
                                    sendMessage(player.getSocket(), "score", getScore(), "all");
                                } else {
                                    //TODO: popup pass massage;
                                }
                            }
                        } else
                            System.out.println("Unknown command");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    for (Player p : gameQueue) {
                        p.setInGame(false);
                        if (p.getSocket() != null) {
                            try {
                                p.getSocket().close();
                                waitingQueue.remove(p);
                                gameQueue.remove(p);
                                System.out.println("Player :" + p.getSocket().toString() + " disconnected");
                                System.out.println("Waiting user: " + waitingQueue.size());
                                System.out.println("In game user: " + gameQueue.size());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        private void initGame(){
            sendMessage(player.getSocket(), "game", "on", "all");
            sendMessage(player.getSocket(), "message", "Game Started", "all");
            sendMessage(player.getSocket(), "message", "You play as white", "me");
            sendMessage(player.getSocket(), "current", algorithm.getCurrentPlayer(), "all");
            sendMessage(player.getSocket(), "show", algorithm.getCurrentMap(), "all");
            sendMessage(player.getSocket(), "score", getScore(), "all");
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

        @Override
        public void getMessage(){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(player.getSocket().getInputStream(), "UTF-8"));
                String text;
                while ((text = bufferedReader.readLine()) != null && !player.isInGame()) {
                    JSONObject jsonGet = new JSONObject(text);
                    if(text.contains("\"command\":")){
                        if(jsonGet.get("command").equals("ready")){
                            if(waitingQueue.contains(player)) {
                                if(gameQueue.size() < 2){   //if game queue has less than 2 player
                                    //if this player is first one in queue, get black, otherwise get white
                                    player.setColor((gameQueue.size() == 0) ? 1 : -1);
                                    gameQueue.add(player);
                                    waitingQueue.remove(player);
                                    if(gameQueue.size() == 1) {
                                        sendMessage(player.getSocket(), "message", "You play as black\nWaiting for white...", "me");
                                        break;
                                    }
                                    if(gameQueue.size() == 2){  //if game queue has two players, start a game
                                        for(Player p : gameQueue)
                                            p.setInGame(true);
                                        initGame(); //initialize game map
                                        break;
                                    }
                                }
                                else{
                                    sendMessage(player.getSocket(), "game", "off", "all");
                                    //TODO: popup <there's a game running, waiting...>
                                }
                            }
                            else
                                System.out.println("Player disappeared from waiting queue!!!");
                        }
                        else if(jsonGet.get("command").toString().equals("surrender")){
                            //TODO: popup <Are you sure to surrender?>
                            for(Player p : gameQueue){
                                p.setInGame(false);
                                waitingQueue.add(p);
                                gameQueue.remove(p);
                            }
                            sendMessage(player.getSocket(), "game", "off", "all");
                        }else if(jsonGet.get("command").toString().equals("notReady")){
                            for(Player p : gameQueue){
                                p.setInGame(false);
                                waitingQueue.add(p);
                                gameQueue.remove(p);
                            }
                        }
                        else
                            System.out.println("Unknown command");
                    }
                    else
                        break;
                }
                new Thread(new Game()).start(); //new thread for a game
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
            }
        }

        @Override
        public void sendMessage(Socket socket, String key, Object value, String who){
            try {
                if(who.equals("all")) {
                    for (Player p : gameQueue) {
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
                }
                else if(who.equals("me")){
                    if (socket.isConnected()) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(key, value);
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
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
    }
}