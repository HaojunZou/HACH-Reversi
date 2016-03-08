package Server;

import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;

public class ReversiServer {
    private static ArrayList<Socket> clients = new ArrayList<Socket>();	//list to save all client socket
    private	BufferedReader bufferedReader;
    private	BufferedWriter bufferedWriter;
    private Algorithm algorithm = new Algorithm();
    private JSONObject jsonObject;

    //constructor
    public ReversiServer(int port){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Reversi Server on...");
            for(;;){
                Socket client = serverSocket.accept();
                clients.add(client);
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

    public class ClientThread implements Runnable{
        Socket clientSocket;
        public ClientThread(Socket socket){
            this.clientSocket = socket;
        }
        @Override
        public void run(){
            if(clientSocket.isConnected()) {
                getMessage(clientSocket);
            }
            if(clientSocket.isClosed()) {
                clients.remove(clientSocket);
            }
        }
    }

    private void getMessage(final Socket clientSocket){
        new Thread(){
            @Override
            public void run() {
                if(clientSocket.isConnected()) {
                    try {
                        bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                        bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                        String text;
                        while ((text = bufferedReader.readLine()) != null) {
                            jsonObject = new JSONObject(text);
                            if(text.contains("\"move\"")) {
                                System.out.println(jsonObject.get("move") + " :: " + jsonObject.get("move").getClass());
                                algorithm.move(clientSocket,
                                        Integer.parseInt(jsonObject.get("move").toString().replace("[", "").replace("]", "").split(",")[0]),
                                        Integer.parseInt(jsonObject.get("move").toString().replace("[", "").replace("]", "").split(",")[1]));
                                sendMessage(clientSocket, "show", algorithm.getCurrentMap(), "all");
                            } else if(text.contains("\"command\"")) {
                                System.out.println(jsonObject.get("command"));
                                if(jsonObject.get("command").equals("ready")){
                                    int [] score = new int[2];
                                    score[0] = algorithm.getCountBlack();
                                    score[1] = algorithm.getCountWhite();
                                    sendMessage(clientSocket, "show", algorithm.getCurrentMap(), "all");
                                    sendMessage(clientSocket, "score", score, "all");
                                    sendMessage(clientSocket, "message", "Welcome to HC-Reversi", "me");
                                    algorithm.setUserFace(clientSocket);
                                    sendMessage(clientSocket, "color", algorithm.getUserFace(clientSocket), "me");
                                    System.out.println(algorithm.getUserFace(clientSocket));
                                    System.out.println(clientSocket.toString());
                                }
                            }
                            else
                                break;
                        }
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(clientSocket.isClosed()){
                        System.out.printf("Client disconnected: %s\n", clientSocket.getRemoteSocketAddress().toString());
                    }
                }
            }
        }.start();
    }

    private void sendMessage(Socket socket, String key, Object value, String who){
        try {
            if(who.equals("all")) {
                for (Socket client : clients) {
                    if (client.isConnected()) {
                        jsonObject = new JSONObject();
                        jsonObject.put(key, value);
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8"));
                        String jsonString = jsonObject.toString();
                        bufferedWriter.write(jsonString);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                }
            }
            if(who.equals("me")){
                jsonObject = new JSONObject();
                jsonObject.put(key, value);
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                String jsonString = jsonObject.toString();
                bufferedWriter.write(jsonString);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}