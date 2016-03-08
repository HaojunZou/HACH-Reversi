//package Client;
//
//import Server.*;
//import org.json.JSONObject;
//
//import java.io.*;
//import java.net.*;
//import java.util.*;
//
//public class MessageBoy{
//    private Socket socket = null;
//    private JSONObject jsonObj;
//    private	BufferedReader bufferedReader;
//    private	BufferedWriter bufferedWriter;
//
//    public void getMessage(final Socket socket){
//        new Thread(){
//            @Override
//            public void run() {
//                try {
//                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
//                    String text;
//                    while(socket.isConnected()) {
//                        if ((text = bufferedReader.readLine()) != null) {
//                            jsonObj = new JSONObject(text);
//                            if (text.contains("\"show\"")) {
//                                System.out.println("show: " + jsonObj.get("show"));
//                            } else if (text.contains("\"message\"")) {
//                                System.out.println("message: " + jsonObj.get("message"));
//                            } else if (text.contains("\"score\"")) {
//                                System.out.println("score: " + jsonObj.get("score"));
//                            } else
//                                break;
//                        }
//                        else
//                            break;
//                    }
//                    socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }finally {
//                    try {
//                        socket.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }.start();
//    }
//
//    public void sendMessage(Socket socket, String key, Object value){
//        new Thread(){
//            @Override
//            public void run() {
//                try {
//                    jsonObj = new JSONObject();
//                    jsonObj.put(key, value);
//                    bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
//                    String jsonString = jsonObj.toString();
//                    bufferedWriter.write(jsonString);
//                    bufferedWriter.newLine();
//                    bufferedWriter.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }
//
//}
