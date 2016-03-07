package Server;

import java.net.*;
import java.io.*;

class ReversiClient implements Runnable{
    private Socket socket = null;
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;
    private String clientIP;
    private JSONPost json = new JSONPost();
    private Algorithm alg = new Algorithm();

    ReversiClient(Socket s){
        this.socket = s;
        this.clientIP = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run(){
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            //response("Welcome to HC-Reversi");
//            ISend sender = new MessageBoy();
            json.send(socket, "show", alg.map());
            json.send(socket, "score", 1);
            json.send(socket, "message", "Chirstelle");

            while(socket.isConnected()){
                //TODO: receive position and give map back
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { if (bufferedReader != null) bufferedReader.close(); } catch (IOException e) { e.printStackTrace(); }
            try { if (bufferedWriter != null) bufferedWriter.close(); } catch (IOException e) { e.printStackTrace(); }
            try { if (socket != null) socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
        System.out.println(clientIP + " disconnected.");
    }

    private void response(String s){
        if (socket.isConnected()) {
            try {
                bufferedWriter.write(s + "\r\n");
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
