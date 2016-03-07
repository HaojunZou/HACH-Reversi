package Server;

import java.io.*;
import java.net.*;
import java.util.*;

class ReversiServer{
    private static ArrayList<Socket> clients = new ArrayList<>();

    private ReversiServer(int port){
        try(ServerSocket server = new ServerSocket(port)){
            System.out.println("Server on...");
            for(;;){
                Socket client = server.accept();
                clients.add(client);
                System.out.printf("Client connected: %s\n", client.getRemoteSocketAddress().toString());
                Thread t = new Thread(new ReversiClient(client));
                t.start();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String [] args){
        if(args.length != 1) {
            System.out.println("Please control program arguments:\n[port]\n");
        }
        else {
            final String PORT = args[0];
            new ReversiServer(Integer.parseInt(PORT));
        }
    }
}