package Server;

import org.json.JSONObject;

import java.io.*;
import java.net.*;

/*
* Responssible for socket connection, disconnection, switch between different Json record and send to different classes.
*/
public class MessageBoy {
    JSONObject jsonObj;
    public MessageBoy(){

    }

    public Object get(Socket socket, String key) {
        Object o = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
//            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream()); //data input stream
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();      //byte output stream
//            byte[] buf = new byte[2048];
//            int line;
            String line;
            while((line = bufferedReader.readLine()) != null){
//                byteArrayOutputStream.write(buf, 0, line);
//                String inputString = new String(byteArrayOutputStream.toByteArray());  //convert to byte array and then to string
                jsonObj = new JSONObject(line);  //initialize a JSON object
                o = jsonObj.get(key);
                System.out.println(o + " : " + o.getClass());
            }
            socket.shutdownInput();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return o;
    }

    public void send(Socket socket, String key, Object object) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream()); //data output stream
            jsonObj = new JSONObject();                 //initialize a JSON object
            jsonObj.put(key, object);                   //put key and value to JSON object
            String jsonString = jsonObj.toString();     //convert JSON object to string
            //byte[] jsonByte = jsonString.getBytes();    //convert string to byte array
            dataOutputStream.writeUTF(jsonString);           //send byte array
            dataOutputStream.flush();
            System.out.println("JSON sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
