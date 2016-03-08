//package Server;
//
//import org.json.JSONObject;
//
//import java.io.*;
//import java.net.*;
//import java.util.Arrays;
//
//public class MessageBoy{
//    private Socket socket = null;
//    private JSONObject jsonObj;
//    private Algorithm algorithm = new Algorithm();
//    private	BufferedReader bufferedReader;
//    private	BufferedWriter bufferedWriter;
//
//    public void getMessage(Socket socket){
//        new Thread(){
//            @Override
//            public void run() {
//                try {
//                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
//                    String text;
//                    while(socket.isConnected()) {
//                        if ((text = bufferedReader.readLine()) != null) {
//                            jsonObj = new JSONObject(text);
//                            if (text.contains("\"move\"")) {
//                                //pieceMap.append(jsonObj.get("show").toString());
//                            } else if(text.contains("\"command\"")){
//                                if(jsonObj.get("command").equals("ready")){
//                                    sendMessage(socket, "show", "map");
//                                    sendMessage(socket, "score", "score");
//                                    sendMessage(socket, "message", "message");
//                                }
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
