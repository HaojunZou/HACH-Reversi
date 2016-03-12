package Server;

import java.net.Socket;

public interface IReversi {
    public void getMessage();
    public void sendMessage(Socket socket, String key, Object value, String who);
}
