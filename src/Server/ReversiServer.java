package Server;

import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;

public class ReversiServer {
    private static LinkedList<Socket> clients = new LinkedList<Socket>();	//list of all clients are online
    private static LinkedList<Socket> waitingQueue = new LinkedList<Socket>();	//list of all clients are waiting
    private static LinkedList<Socket> gameQueue = new LinkedList<Socket>();	//list of all clients are engaging
    private Algorithm algorithm = new Algorithm();

    //constructor
    private ReversiServer(int port){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Reversi Server on...");
            for(;;){
                Socket client = serverSocket.accept();
                clients.add(client);
                waitingQueue.add(client);
                System.out.printf("Client connected: %s\n", client.getRemoteSocketAddress().toString());
                Thread t = new Thread(new ClientThread(client));    //build a new thread to client
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
        Socket socket;
        Player player = null;
        boolean inGame = false;

        ClientThread(Socket s){
            this.socket = s;
        }

        @Override
        public void run(){
            if(socket.isClosed()) {
                clients.remove(socket);
                waitingQueue.remove(socket);
                gameQueue.remove(socket);
            }
            sendMessage("message", "--- Welcome to HC-Reversi ---", "me");
            getMessage();
        }

        class Game implements Runnable{
            Game(){
            }
            @Override
            public void run() {
                if (socket.isConnected()) {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
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
//                                    if(algorithm.checkLegal()) {
                                        sendMessage("message",
                                                (algorithm.getCurrentPlayer() == 1) ?
                                                        ("Black " + "[" + getX(x) + "," + (y + 1) + "]")
                                                        : ("White " + "[" + getX(x) + "," + (y + 1) + "]"),
                                                "all");
//                                    }
                                        algorithm.move(x, y);   //current player might change after this move
                                        sendMessage("current", algorithm.getCurrentPlayer(), "all");
                                        sendMessage("show", algorithm.getCurrentMap(), "all");
                                        sendMessage("score", getScore(), "all");
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
                        inGame = false;
                    }
                }
            }
        }

        private void initGame(){
            sendMessage("game", "on", "all");
            sendMessage("message", "Game Started", "all");
            sendMessage("message", "You play as white", "me");
            sendMessage("current", algorithm.getCurrentPlayer(), "all");
            sendMessage("show", algorithm.getCurrentMap(), "all");
            sendMessage("score", getScore(), "all");
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
            if (socket.isConnected()) {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    String text;
                    while ((text = bufferedReader.readLine()) != null && !inGame) {
                        JSONObject jsonGet = new JSONObject(text);
                        if(text.contains("\"command\":")){
                            if(jsonGet.get("command").equals("ready")){
                                if(waitingQueue.contains(socket)) {
                                    if(gameQueue.size() < 2){   //if game queue has less than 2 player
                                        gameQueue.add(socket);
                                        waitingQueue.remove(socket);
                                        System.out.println(gameQueue.size());
                                        player = new Player();
                                        player.setSocket(socket);
                                        //if this player is first one in queue, get black, otherwise get white
                                        player.setColor((gameQueue.size() == 1) ? 1 : -1);
                                        if(gameQueue.size() == 1) {
                                            sendMessage("message", "You play as black\nWaiting for white...", "me");
                                            break;
                                        }
                                        if(gameQueue.size() == 2){  //if game queue has two players, start a game
                                            inGame = true;
                                            initGame(); //initialize game map
                                            break;
                                        }
                                    }
                                    else{
                                        sendMessage("game", "off", "all");
                                        //TODO: popup <there's a game running, waiting...>
                                    }
                                }
                                else
                                    System.out.println("Player disappeared from waiting queue!!!");
                            }
                            else if(jsonGet.get("command").toString().equals("surrender")){
                                inGame = false;
                                waitingQueue.add(socket);
                                gameQueue.remove(socket);
                                sendMessage("game", "off", "all");
                                //TODO: popup <Are you sure to surrender?>
                            }else if(jsonGet.get("command").toString().equals("notReady")){
                                inGame = false;
                                waitingQueue.add(socket);
                                gameQueue.remove(socket);
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
        }

        @Override
        public void sendMessage(String key, Object value, String who){
            try {
                if(who.equals("all")) {
                    for (Socket socket : gameQueue) {
                        if (socket.isConnected()) {
                            JSONObject jsonSend = new JSONObject();
                            jsonSend.put(key, value);
                            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
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