package Server;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

class JSONPost {
    private JSONObject jsonObj;

    //constructor
    JSONPost(){
    }

    void send(Socket socket, String key, Object object){
        try {
            jsonObj = new JSONObject();
            jsonObj.put(key, object);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            String jsonString = jsonObj.toString();
            bufferedWriter.write(jsonString);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            System.out.println("JSON sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Object get(Socket socket, String key){
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            String len;
            if((len = bufferedReader.readLine()) != null){
                jsonObj = new JSONObject(len);
                return jsonObj.get(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
