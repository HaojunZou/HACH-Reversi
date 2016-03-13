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
                            waitingQueue.remove(player);
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
                    waitingQueue.remove(player);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Client " + player.getSocket().toString() + " Disconnected");
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
                            waitingQueue.remove(player);
                            if(gameQueue.size() == 2)  //if game queue has two players, start a game
                                gameStart();
                        }
                    } else if(jsonGet.get("command").equals("notReady")){
                        waitingQueue.add(player);
                        gameQueue.remove(player);
                    } else if(jsonGet.get("command").toString().equals("surrender")){
                        sendMessage(player.getSocket(), "game", "off", "all");
                        //no matter who has more piece in the map, surrender will affect the opponent wins
                        sendMessage(player.getSocket(), "message", (player.getColor()==1) ? "White wins" : "Black wins", "all");
                        waitingQueue.add(gameQueue.getFirst());
                        waitingQueue.add(gameQueue.getLast());
                        gameQueue.clear();
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
                System.out.println("Unknown command");
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
                        waitingQueue.add(p);
                        sendMessage(p.getSocket(), "game", "off", "me");
                    }
                    gameQueue.clear();
                    System.out.println("Waiting user: " + waitingQueue.size());
                    System.out.println("In game user: " + gameQueue.size());
                    algorithm = new Algorithm();
                    gameOver = false;
                }
            }
        }   //end of Game thread
    }   //end of Client thread
}